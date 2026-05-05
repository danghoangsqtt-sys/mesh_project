from fastapi import APIRouter, BackgroundTasks, HTTPException
from pydantic import BaseModel
from typing import Optional

from app.services.map_service import map_service

router = APIRouter()

class DownloadMapRequest(BaseModel):
    min_lon: float
    min_lat: float
    max_lon: float
    max_lat: float
    source_url: Optional[str] = None

@router.post("/download")
async def download_pmtiles_map(request: DownloadMapRequest, background_tasks: BackgroundTasks):
    """
    Start a background task to download/extract PMTiles map data for the given bounding box.
    """
    if map_service.is_downloading:
        raise HTTPException(status_code=400, detail="A map download is already in progress.")
        
    import socket
    try:
        socket.create_connection(("8.8.8.8", 53), timeout=3)
    except OSError:
        raise HTTPException(status_code=400, detail="Hệ thống (Pi) không có kết nối Internet. Vui lòng cắm cáp mạng LAN hoặc kết nối Wi-Fi có Internet cho Pi.")
        
    background_tasks.add_task(
        map_service.download_pmtiles,
        request.min_lon,
        request.min_lat,
        request.max_lon,
        request.max_lat,
        request.source_url
    )
    
    return {"status": "ok", "message": "Map download started in background. Listen to WebSocket for progress."}

@router.get("/status")
async def get_map_status():
    """Check if map is currently downloading"""
    return {
        "is_downloading": map_service.is_downloading
    }
