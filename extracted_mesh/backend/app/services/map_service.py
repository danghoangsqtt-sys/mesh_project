import os
import asyncio
import httpx
import time
import socket
from app.ws.connection_manager import manager
from datetime import datetime, timedelta

# Ensure MAPS_DIR exists
MAPS_DIR = "/opt/mesh_pi5_server/frontend/dist/maps"
if not os.path.exists(MAPS_DIR):
    # Fallback for local development
    MAPS_DIR = os.path.join(os.path.dirname(__file__), "../../../frontend/dist/maps")
    os.makedirs(MAPS_DIR, exist_ok=True)

class MapService:
    def __init__(self):
        self.is_downloading = False

    async def get_latest_protomaps_url(self) -> str:
        """Find the latest valid Protomaps daily build URL."""
        date = datetime.utcnow()
        async with httpx.AsyncClient() as client:
            # Check up to 5 days back
            for i in range(5):
                date_str = (date - timedelta(days=i)).strftime("%Y%m%d")
                url = f"https://build.protomaps.com/{date_str}.pmtiles"
                try:
                    resp = await client.head(url, timeout=3.0)
                    if resp.status_code == 200:
                        return url
                except Exception:
                    pass
        # Ultimate fallback
        return "https://build.protomaps.com/20260505.pmtiles"

    async def download_pmtiles(self, min_lon: float, min_lat: float, max_lon: float, max_lat: float, source_url: str = None):
        """
        Background task to download/extract PMTiles map data.
        """
        if self.is_downloading:
            return
            
        self.is_downloading = True
        try:
            # 1. Notify start
            await manager.broadcast({
                "type": "map_download_progress",
                "data": {"progress": 0, "status": "Đang lấy dữ liệu bản đồ..."}
            })

            import shutil
            pmtiles_bin = shutil.which("pmtiles")
            
            # Dynamically get the latest global dataset if no source is provided
            target_url = source_url or await self.get_latest_protomaps_url()
            dest_file = os.path.join(MAPS_DIR, "offline.pmtiles")
            temp_file = dest_file + ".tmp"

            # Ensure directory exists right before downloading
            os.makedirs(MAPS_DIR, exist_ok=True)

            if os.path.exists(temp_file):
                os.remove(temp_file)

            if pmtiles_bin:
                # --- AUTO EXTRACTION VIA PMTILES CLI ---
                await manager.broadcast({
                    "type": "map_download_progress",
                    "data": {"progress": 10, "status": f"Đang cắt bản đồ từ {target_url}..."}
                })
                
                cmd = [
                    pmtiles_bin, 
                    "extract", 
                    target_url, 
                    temp_file, 
                    f"--bbox={min_lon},{min_lat},{max_lon},{max_lat}"
                ]
                
                process = await asyncio.create_subprocess_exec(
                    *cmd,
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                
                # Simulate progress while waiting
                for i in range(15, 95, 5):
                    if process.returncode is not None:
                        break
                    await manager.broadcast({
                        "type": "map_download_progress",
                        "data": {"progress": i, "status": f"Đang tải & cắt dữ liệu Vector ({i}%)..."}
                    })
                    try:
                        await asyncio.wait_for(process.wait(), timeout=2.0)
                    except asyncio.TimeoutError:
                        pass
                
                await process.wait()
                
                if process.returncode != 0:
                    stderr = (await process.stderr.read()).decode()
                    stdout = (await process.stdout.read()).decode()
                    raise Exception(f"pmtiles extract failed: {stderr} | {stdout}")

            else:
                # --- FALLBACK: DIRECT HTTP DOWNLOAD ---
                target_url = source_url or await self.get_latest_protomaps_url()
                
                # Disable timeout for large file downloads on slow connections
                async with httpx.AsyncClient(follow_redirects=True, timeout=None) as client:
                    async with client.stream("GET", target_url) as response:
                        response.raise_for_status()
                        total_bytes = int(response.headers.get("content-length", 1000000))  # fallback 1MB
                        downloaded_bytes = 0
                        
                        last_broadcast_time = time.time()
                        
                        with open(temp_file, "wb") as f:
                            # Use 256KB chunks
                            async for chunk in response.aiter_bytes(chunk_size=262144):
                                f.write(chunk)
                                downloaded_bytes += len(chunk)
                                
                                # Broadcast progress every 0.2s
                                current_time = time.time()
                                if current_time - last_broadcast_time > 0.2:
                                    progress = min(99, int((downloaded_bytes / total_bytes) * 100))
                                    await manager.broadcast({
                                        "type": "map_download_progress",
                                        "data": {"progress": progress, "status": f"Downloading Map Data... {progress}%"}
                                    })
                                    last_broadcast_time = current_time

            # Rename temp to final
            os.replace(temp_file, dest_file)
            
            # Ensure permissions so Nginx can read it
            try:
                os.chmod(dest_file, 0o644)
            except Exception:
                pass

            # 2. Notify complete
            await manager.broadcast({
                "type": "map_download_progress",
                "data": {"progress": 100, "status": "Download Complete!"}
            })
            
            # Trigger frontend to reload map
            await manager.broadcast({
                "type": "map_ready"
            })

        except Exception as e:
            await manager.broadcast({
                "type": "map_download_progress",
                "data": {"progress": -1, "status": f"Error: {str(e)}"}
            })
        finally:
            self.is_downloading = False

map_service = MapService()
