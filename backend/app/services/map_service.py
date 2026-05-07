import asyncio
import os
import subprocess
from app.ws.connection_manager import manager

# Maps stored outside dist/ so they survive frontend rebuilds
MAP_STORAGE_DIR = "/opt/mesh_pi5_server/maps"
MAP_FILE_NAME = "offline.pmtiles"

PMTILES_INSTALL_CMD = (
    "curl -L https://github.com/protomaps/go-pmtiles/releases/download/v1.20.0/"
    "go-pmtiles_1.20.0_Linux_arm64.tar.gz | tar xz && sudo mv pmtiles /usr/local/bin/"
)


def _progress(progress: int, status: str) -> dict:
    return {"progress": progress, "status": status}


class MapService:
    def __init__(self):
        self.downloading = False
        self._lock: asyncio.Lock | None = None

    @property
    def is_downloading(self) -> bool:
        return self.downloading

    def _get_lock(self) -> asyncio.Lock:
        if self._lock is None:
            self._lock = asyncio.Lock()
        return self._lock

    async def download_pmtiles(self, min_lon, min_lat, max_lon, max_lat, source_url):
        async with self._get_lock():
            if self.downloading:
                return
            self.downloading = True
        try:
            pmtiles_path = "/usr/local/bin/pmtiles" if os.path.exists("/usr/local/bin/pmtiles") else "pmtiles"

            check = await asyncio.create_subprocess_exec(
                pmtiles_path, "version",
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
            )
            await check.communicate()
            if check.returncode != 0:
                await manager.broadcast_event("map_download_progress", _progress(
                    -1, f"Lỗi: Không tìm thấy 'pmtiles'. Cài đặt bằng lệnh:\n{PMTILES_INSTALL_CMD}"
                ))
                return

            os.makedirs(MAP_STORAGE_DIR, exist_ok=True)
            dest_file = os.path.join(MAP_STORAGE_DIR, MAP_FILE_NAME)

            await manager.broadcast_event("map_download_progress", _progress(5, "Bắt đầu trích xuất PMTiles..."))

            cmd = [
                pmtiles_path, "extract",
                source_url,
                dest_file,
                f"--bbox={min_lon},{min_lat},{max_lon},{max_lat}",
            ]

            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
            )

            progress = 10
            while True:
                try:
                    await asyncio.wait_for(process.wait(), timeout=2.0)
                    break
                except asyncio.TimeoutError:
                    progress = min(90, progress + 2)
                    await manager.broadcast_event("map_download_progress", _progress(
                        progress, "Đang tải dữ liệu từ Protomaps..."
                    ))

            stdout, stderr = await process.communicate()

            if process.returncode == 0:
                await manager.broadcast_event("map_download_progress", _progress(100, "Tải thành công!"))
            else:
                error_msg = (
                    stderr.decode("utf-8", errors="replace").strip()
                    or stdout.decode("utf-8", errors="replace").strip()
                )
                await manager.broadcast_event("map_download_progress", _progress(-1, f"Lỗi tải: {error_msg}"))

        except FileNotFoundError:
            await manager.broadcast_event("map_download_progress", _progress(
                -1, f"Thiếu công cụ 'pmtiles'. Cài đặt:\n{PMTILES_INSTALL_CMD}"
            ))
        except Exception as e:
            await manager.broadcast_event("map_download_progress", _progress(-1, f"Lỗi hệ thống: {str(e)}"))
        finally:
            self.downloading = False

    @property
    def map_file_path(self) -> str:
        return os.path.join(MAP_STORAGE_DIR, MAP_FILE_NAME)

    @property
    def map_exists(self) -> bool:
        return os.path.isfile(self.map_file_path)


map_service = MapService()
