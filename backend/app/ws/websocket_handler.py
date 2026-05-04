"""
Mesh Pi5 Server — WebSocket endpoint

File: ws/websocket_handler.py
Description: WebSocket endpoint that streams real-time mesh data to browser clients.
"""

import asyncio
import logging

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app.services.serial_bridge import serial_bridge
from app.ws.connection_manager import manager

logger = logging.getLogger(__name__)

router = APIRouter()


async def _packet_broadcaster():
    """Background task: consume packets from serial bridge queue and broadcast."""
    while True:
        try:
            packet = await serial_bridge.packet_queue.get()
            await manager.broadcast_packet(packet)
        except asyncio.CancelledError:
            break
        except Exception as e:
            logger.error("Error broadcasting packet: %s", e)


# Will be started as a background task when first client connects
_broadcaster_task: asyncio.Task | None = None


def _ensure_broadcaster():
    """Ensure the packet broadcaster background task is running."""
    global _broadcaster_task
    if _broadcaster_task is None or _broadcaster_task.done():
        _broadcaster_task = asyncio.create_task(_packet_broadcaster())


@router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """WebSocket endpoint for real-time mesh data streaming."""
    await manager.connect(websocket)
    _ensure_broadcaster()

    try:
        while True:
            # Keep connection alive; handle incoming messages from client
            data = await websocket.receive_text()
            # Future: handle client commands via WebSocket
            logger.debug("Received from client: %s", data)
    except WebSocketDisconnect:
        await manager.disconnect(websocket)
    except Exception as e:
        logger.error("WebSocket error: %s", e)
        await manager.disconnect(websocket)
