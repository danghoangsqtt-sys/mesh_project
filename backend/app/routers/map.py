import os
from fastapi import APIRouter, BackgroundTasks, HTTPException
from pydantic import BaseModel
from typing import Optional
from app.services.map_service import map_service, MAP_STORAGE_DIR, MAP_FILE_NAME

router = APIRouter()

DEFAULT_SOURCE = "https://data.source.coop/protomaps/openstreetmap/tiles/v3.pmtiles"



class MapDownloadRequest(BaseModel):
    min_lon: float
    min_lat: float
    max_lon: float
    max_lat: float
    source_url: Optional[str] = DEFAULT_SOURCE


@router.get("/status")
async def map_status():
    map_path = os.path.join(MAP_STORAGE_DIR, MAP_FILE_NAME)
    exists = os.path.isfile(map_path)
    size_mb = round(os.path.getsize(map_path) / (1024 * 1024), 1) if exists else 0
    return {
        "exists": exists,
        "path": map_path,
        "size_mb": size_mb,
        "downloading": map_service.downloading,
    }


@router.post("/download")
async def start_download(req: MapDownloadRequest, background_tasks: BackgroundTasks):
    if map_service.downloading:
        raise HTTPException(status_code=400, detail="Một tiến trình tải bản đồ đang chạy.")

    source = req.source_url or DEFAULT_SOURCE

    background_tasks.add_task(
        map_service.download_pmtiles,
        req.min_lon, req.min_lat, req.max_lon, req.max_lat, source,
    )
    return {"status": "started", "message": "Bắt đầu tải bản đồ"}
