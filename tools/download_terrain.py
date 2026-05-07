#!/usr/bin/env python3
"""
Download AWS Open Data Terrain Tiles (Terrarium RGB) -> MBTiles
For 3D Terrain in MapLibre offline maps on Raspberry Pi 5.

Usage: python download_terrain.py
"""
import math
import os
import sqlite3
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed

# ─── Configuration ─────────────────────────────────────────
# Nha Trang + surrounding area (tactical coverage)
BBOX = {
    'min_lon': 109.05,
    'min_lat': 12.15,
    'max_lon': 109.30,
    'max_lat': 12.35
}

# Terrain zoom levels (0-14 is usually sufficient for high-detail DEM)
# MapLibre will automatically overzoom DEM tiles.
ZOOM_MIN = 0
ZOOM_MAX = 13

# AWS Open Data Registry - Mapzen Terrarium format
TILE_URL = "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png"

OUTPUT_FILE = os.path.join(os.path.dirname(__file__), "terrain.mbtiles")

MAX_WORKERS = 8
USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
RETRY_COUNT = 3
TIMEOUT = 10

# ─── Tile Math ─────────────────────────────────────────────
def deg2num(lat_deg, lon_deg, zoom):
    lat_rad = math.radians(lat_deg)
    n = 2.0 ** zoom
    xtile = int((lon_deg + 180.0) / 360.0 * n)
    ytile = int((1.0 - math.asinh(math.tan(lat_rad)) / math.pi) / 2.0 * n)
    return max(0, min(int(n - 1), xtile)), max(0, min(int(n - 1), ytile))

def get_tile_range(bbox, zoom):
    x1, y1 = deg2num(bbox['max_lat'], bbox['min_lon'], zoom)
    x2, y2 = deg2num(bbox['min_lat'], bbox['max_lon'], zoom)
    return min(x1, x2), min(y1, y2), max(x1, x2), max(y1, y2)

def estimate_tiles(bbox, zoom_min, zoom_max):
    total = 0
    for z in range(zoom_min, zoom_max + 1):
        x1, y1, x2, y2 = get_tile_range(bbox, z)
        total += (x2 - x1 + 1) * (y2 - y1 + 1)
    return total

def flip_y(y, z):
    """Convert XYZ to TMS for MBTiles"""
    return (2 ** z) - 1 - y

# ─── MBTiles Database ──────────────────────────────────────
def init_mbtiles(filepath):
    conn = sqlite3.connect(filepath)
    c = conn.cursor()
    c.execute("""
        CREATE TABLE IF NOT EXISTS tiles (
            zoom_level INTEGER,
            tile_column INTEGER,
            tile_row INTEGER,
            tile_data BLOB,
            PRIMARY KEY (zoom_level, tile_column, tile_row)
        )
    """)
    c.execute("""
        CREATE TABLE IF NOT EXISTS metadata (
            name TEXT PRIMARY KEY,
            value TEXT
        )
    """)
    metadata = {
        'name': 'Nha Trang 3D Terrain',
        'format': 'png',
        'type': 'baselayer',
        'description': 'Terrarium RGB DEM for MapLibre 3D Terrain',
        'bounds': f"{BBOX['min_lon']},{BBOX['min_lat']},{BBOX['max_lon']},{BBOX['max_lat']}",
        'center': f"{(BBOX['min_lon']+BBOX['max_lon'])/2},{(BBOX['min_lat']+BBOX['max_lat'])/2},{ZOOM_MAX}",
        'minzoom': str(ZOOM_MIN),
        'maxzoom': str(ZOOM_MAX),
    }
    for k, v in metadata.items():
        c.execute("INSERT OR REPLACE INTO metadata (name, value) VALUES (?, ?)", (k, v))
    conn.commit()
    return conn

# ─── Downloader ───────────────────────────────────────────
def download_tile(z, x, y):
    url = TILE_URL.format(z=z, x=x, y=y)
    for attempt in range(RETRY_COUNT + 1):
        try:
            req = urllib.request.Request(url, headers={'User-Agent': USER_AGENT})
            with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
                data = resp.read()
                if len(data) > 0:
                    return (z, x, y, data)
                return (z, x, y, None)
        except Exception as e:
            if attempt < RETRY_COUNT:
                time.sleep(1)
            else:
                return (z, x, y, None)
    return (z, x, y, None)

def main():
    total_tiles = estimate_tiles(BBOX, ZOOM_MIN, ZOOM_MAX)
    print(f"\n=== TERRAIN 3D TILE DOWNLOADER ===")
    print(f"  Source: AWS Open Data (Terrarium DEM)")
    print(f"  Area: {BBOX['min_lon']:.2f},{BBOX['min_lat']:.2f} -> {BBOX['max_lon']:.2f},{BBOX['max_lat']:.2f}")
    print(f"  Zoom: {ZOOM_MIN} -> {ZOOM_MAX}")
    print(f"  Total tiles: {total_tiles:,}")
    print(f"==================================\n")

    conn = init_mbtiles(OUTPUT_FILE)
    cursor = conn.cursor()

    cursor.execute("SELECT COUNT(*) FROM tiles")
    existing = cursor.fetchone()[0]
    if existing > 0:
        print(f"[*] Found {existing:,} existing tiles in database.")

    downloaded = 0
    skipped = 0
    failed = 0
    start_time = time.time()
    batch_data = []

    for z in range(ZOOM_MIN, ZOOM_MAX + 1):
        x1, y1, x2, y2 = get_tile_range(BBOX, z)
        zoom_tiles = (x2 - x1 + 1) * (y2 - y1 + 1)
        print(f"\n[Zoom {z}] {zoom_tiles:,} tiles")

        tasks = []
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                tms_y = flip_y(y, z)
                cursor.execute("SELECT 1 FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?", (z, x, tms_y))
                if cursor.fetchone():
                    skipped += 1
                else:
                    tasks.append((z, x, y))

        if not tasks:
            continue

        with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
            futures = {executor.submit(download_tile, z, x, y): (z, x, y) for z, x, y in tasks}
            for future in as_completed(futures):
                z_r, x_r, y_r, data = future.result()
                if data:
                    tms_y = flip_y(y_r, z_r)
                    batch_data.append((z_r, x_r, tms_y, data))
                    downloaded += 1

                    if len(batch_data) >= 50:
                        cursor.executemany("INSERT OR REPLACE INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)", batch_data)
                        conn.commit()
                        batch_data = []
                else:
                    failed += 1

                processed = downloaded + skipped + failed
                if processed % 10 == 0 or processed == total_tiles:
                    pct = processed / total_tiles * 100
                    print(f"   [{pct:5.1f}%] DL: {downloaded} | Skip: {skipped} | Fail: {failed}", end='\r')

        if batch_data:
            cursor.executemany("INSERT OR REPLACE INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)", batch_data)
            conn.commit()
            batch_data = []

    conn.close()
    elapsed = time.time() - start_time
    size_mb = os.path.getsize(OUTPUT_FILE) / (1024*1024)

    print(f"\n\n=== COMPLETE ===")
    print(f"  Downloaded: {downloaded}")
    print(f"  Failed:     {failed}")
    print(f"  Time:       {elapsed:.1f}s")
    print(f"  Size:       {size_mb:.1f} MB")
    print(f"  Next step:  pmtiles convert terrain.mbtiles terrain.pmtiles")

if __name__ == "__main__":
    main()
