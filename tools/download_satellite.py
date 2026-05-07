#!/usr/bin/env python3
"""
Download ESRI World Imagery satellite tiles → MBTiles
For tactical offline map on Raspberry Pi 5

Usage: python download_satellite.py
"""
import math
import os
import sqlite3
import sys
import time
import urllib.request
import json
from concurrent.futures import ThreadPoolExecutor, as_completed

# ─── Configuration ─────────────────────────────────────────
# Nha Trang + surrounding area (tactical coverage)
BBOX = {
    'min_lon': 109.05,
    'min_lat': 12.15,
    'max_lon': 109.30,
    'max_lat': 12.35
}

# Zoom levels: 0-16 for good satellite detail (each building visible)
ZOOM_MIN = 0
ZOOM_MAX = 16

# ESRI World Imagery (highest quality free satellite)
TILE_URL = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"

# Output
OUTPUT_FILE = os.path.join(os.path.dirname(__file__), "satellite.mbtiles")

# Download settings
MAX_WORKERS = 8
USER_AGENT = "MeshTacticalMap/1.0 (offline tactical use)"
RETRY_COUNT = 2
TIMEOUT = 15

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

# ─── MBTiles Database ──────────────────────────────────────
def init_mbtiles(filepath):
    """Initialize MBTiles SQLite database."""
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
    # MBTiles metadata
    metadata = {
        'name': 'Nha Trang Satellite Imagery',
        'format': 'jpg',
        'type': 'baselayer',
        'description': 'ESRI World Imagery for Nha Trang tactical operations',
        'bounds': f"{BBOX['min_lon']},{BBOX['min_lat']},{BBOX['max_lon']},{BBOX['max_lat']}",
        'center': f"{(BBOX['min_lon']+BBOX['max_lon'])/2},{(BBOX['min_lat']+BBOX['max_lat'])/2},{ZOOM_MAX}",
        'minzoom': str(ZOOM_MIN),
        'maxzoom': str(ZOOM_MAX),
    }
    for k, v in metadata.items():
        c.execute("INSERT OR REPLACE INTO metadata (name, value) VALUES (?, ?)", (k, v))
    conn.commit()
    return conn

def flip_y(y, z):
    """Convert XYZ tile y to TMS y (MBTiles uses TMS scheme)."""
    return (2 ** z) - 1 - y

# ─── Tile Downloader ──────────────────────────────────────
def download_tile(z, x, y):
    """Download a single tile. Returns (z, x, y, data) or (z, x, y, None)."""
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
                time.sleep(0.5 * (attempt + 1))
            else:
                return (z, x, y, None)
    return (z, x, y, None)

# ─── Main ─────────────────────────────────────────────────
def main():
    total_tiles = estimate_tiles(BBOX, ZOOM_MIN, ZOOM_MAX)
    print(f"\n=== SATELLITE TILE DOWNLOADER ===")
    print(f"  Nha Trang Tactical Map")
    print(f"  Source: ESRI World Imagery")
    print(f"  Area: {BBOX['min_lon']:.2f},{BBOX['min_lat']:.2f} -> {BBOX['max_lon']:.2f},{BBOX['max_lat']:.2f}")
    print(f"  Zoom: {ZOOM_MIN} -> {ZOOM_MAX}")
    print(f"  Total tiles: {total_tiles:,}")
    print(f"  Output: {os.path.basename(OUTPUT_FILE)}")
    print(f"  Workers: {MAX_WORKERS}")
    print(f"================================\n")

    # Check if output already exists
    if os.path.exists(OUTPUT_FILE):
        size_mb = os.path.getsize(OUTPUT_FILE) / (1024*1024)
        print(f"[!] File {OUTPUT_FILE} already exists ({size_mb:.1f} MB)")
        print("   Continuing will add missing tiles (existing tiles preserved)")

    # Initialize MBTiles
    print("[*] Initializing MBTiles database...")
    conn = init_mbtiles(OUTPUT_FILE)
    cursor = conn.cursor()

    # Check existing tiles
    cursor.execute("SELECT COUNT(*) FROM tiles")
    existing_count = cursor.fetchone()[0]
    if existing_count > 0:
        print(f"   Found {existing_count:,} existing tiles, will skip duplicates")

    downloaded = 0
    skipped = 0
    failed = 0
    start_time = time.time()
    batch_data = []
    BATCH_SIZE = 100

    for z in range(ZOOM_MIN, ZOOM_MAX + 1):
        x1, y1, x2, y2 = get_tile_range(BBOX, z)
        zoom_tiles = (x2 - x1 + 1) * (y2 - y1 + 1)
        print(f"\n[Zoom {z}] {zoom_tiles:,} tiles (x: {x1}-{x2}, y: {y1}-{y2})")

        # Build task list for this zoom
        tasks = []
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                tms_y = flip_y(y, z)
                # Check if tile already exists
                cursor.execute(
                    "SELECT 1 FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?",
                    (z, x, tms_y)
                )
                if cursor.fetchone():
                    skipped += 1
                    continue
                tasks.append((z, x, y))

        if not tasks:
            print(f"   [OK] All tiles already downloaded, skipping")
            continue

        print(f"   [DL] Downloading {len(tasks)} new tiles...")

        # Download with thread pool
        with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
            futures = {executor.submit(download_tile, z, x, y): (z, x, y) for z, x, y in tasks}

            for future in as_completed(futures):
                z_r, x_r, y_r, data = future.result()
                if data:
                    tms_y = flip_y(y_r, z_r)
                    batch_data.append((z_r, x_r, tms_y, data))
                    downloaded += 1

                    # Batch insert
                    if len(batch_data) >= BATCH_SIZE:
                        cursor.executemany(
                            "INSERT OR REPLACE INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)",
                            batch_data
                        )
                        conn.commit()
                        batch_data = []
                else:
                    failed += 1

                # Progress
                processed = downloaded + skipped + failed
                if processed % 200 == 0 or processed == total_tiles:
                    elapsed = time.time() - start_time
                    rate = downloaded / max(elapsed, 0.1)
                    pct = processed / total_tiles * 100
                    print(f"   [{pct:5.1f}%] Downloaded: {downloaded:,} | Skipped: {skipped:,} | Failed: {failed:,} | {rate:.0f} tiles/s", end='\r')

        # Flush remaining batch
        if batch_data:
            cursor.executemany(
                "INSERT OR REPLACE INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)",
                batch_data
            )
            conn.commit()
            batch_data = []

    # Final stats
    elapsed = time.time() - start_time
    file_size = os.path.getsize(OUTPUT_FILE) / (1024 * 1024)
    conn.close()

    print(f"\n\n=== DOWNLOAD COMPLETE ===")
    print(f"  Downloaded: {downloaded:>8,} tiles")
    print(f"  Skipped:    {skipped:>8,} tiles")
    print(f"  Failed:     {failed:>8,} tiles")
    print(f"  Time:       {elapsed:>8.1f} seconds")
    print(f"  File size:  {file_size:>8.1f} MB")
    print(f"  Output:     {os.path.basename(OUTPUT_FILE)}")
    print(f"========================")
    print(f"\nNext step: pmtiles convert {os.path.basename(OUTPUT_FILE)} satellite.pmtiles")

if __name__ == "__main__":
    main()
