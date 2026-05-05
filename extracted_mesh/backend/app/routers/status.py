"""
Mesh Pi5 Server — Status API Router

File: routers/status.py
Description: System status endpoint — serial connection, node count, uptime.
"""

import time
from datetime import datetime

from fastapi import APIRouter, Depends
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.database import get_db
from app.models import SoldierEntity
from app.services.serial_bridge import serial_bridge
from app.ws.connection_manager import manager

router = APIRouter()

_start_time = time.time()


@router.get("/")
async def system_status(db: AsyncSession = Depends(get_db)):
    """Get overall system status."""
    result = await db.execute(select(func.count()).select_from(SoldierEntity))
    node_count = result.scalar() or 0

    uptime_seconds = int(time.time() - _start_time)
    hours, remainder = divmod(uptime_seconds, 3600)
    minutes, seconds = divmod(remainder, 60)

    return {
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "serial": {
            "connected": serial_bridge.is_connected,
            "port": settings.SERIAL_PORT,
            "baudrate": settings.SERIAL_BAUDRATE,
        },
        "gateway_gps": serial_bridge.gateway_gps,
        "nodes": {
            "total": node_count,
        },
        "websocket": {
            "clients": manager.client_count,
        },
        "uptime": f"{hours:02d}:{minutes:02d}:{seconds:02d}",
        "timestamp": datetime.now().isoformat(),
    }

from pydantic import BaseModel

class ConnectRequest(BaseModel):
    port: str
    baudrate: int

@router.post("/connect")
async def connect_serial(req: ConnectRequest):
    """Reconnect serial port with new settings."""
    settings.SERIAL_PORT = req.port
    settings.SERIAL_BAUDRATE = req.baudrate
    
    # Restart the serial bridge reader loop by closing current connection
    # The background loop will auto-reconnect with new settings
    if serial_bridge._serial and serial_bridge._serial.is_open:
        serial_bridge._serial.close()
    serial_bridge.is_connected = False
    
    return {"status": "ok", "message": f"Reconnecting to {req.port} @ {req.baudrate}..."}
