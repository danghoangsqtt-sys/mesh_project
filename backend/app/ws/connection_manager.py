"""
Mesh Pi5 Server — WebSocket Connection Manager

File: ws/connection_manager.py
Description: Manages WebSocket connections, broadcasts packets to all clients.
"""

import asyncio
import json
import logging

from fastapi import WebSocket

from app.services.packet_parser import SoldierPacket

logger = logging.getLogger(__name__)


class ConnectionManager:
    """Manages active WebSocket connections and broadcasts data."""

    def __init__(self):
        self._connections: list[WebSocket] = []
        self._lock = asyncio.Lock()

    async def connect(self, websocket: WebSocket):
        """Accept and register a new WebSocket connection."""
        await websocket.accept()
        async with self._lock:
            self._connections.append(websocket)
        logger.info("WebSocket client connected. Total: %d", len(self._connections))

    async def disconnect(self, websocket: WebSocket):
        """Remove a WebSocket connection."""
        async with self._lock:
            if websocket in self._connections:
                self._connections.remove(websocket)
        logger.info("WebSocket client disconnected. Total: %d", len(self._connections))

    async def disconnect_all(self):
        """Close all WebSocket connections."""
        async with self._lock:
            for ws in self._connections:
                try:
                    await ws.close()
                except Exception:
                    pass
            self._connections.clear()

    async def broadcast_packet(self, packet: SoldierPacket):
        """Broadcast a parsed packet to all connected WebSocket clients."""
        if not self._connections:
            return

        from datetime import datetime
        data = packet.to_dict()
        data["last_seen"] = datetime.now().isoformat()
        data["name"] = None
        message = json.dumps({"type": "node_update", "data": data})

        disconnected: list[WebSocket] = []

        async with self._lock:
            for ws in self._connections:
                try:
                    await ws.send_text(message)
                except Exception:
                    disconnected.append(ws)

        # Clean up disconnected clients
        if disconnected:
            async with self._lock:
                for ws in disconnected:
                    if ws in self._connections:
                        self._connections.remove(ws)
            logger.info("Removed %d dead connections.", len(disconnected))

    async def broadcast_event(self, event_type: str, data: dict):
        """Broadcast a system event to all connected WebSocket clients."""
        if not self._connections:
            return

        message = json.dumps({
            "type": event_type,
            "data": data,
        })

        disconnected: list[WebSocket] = []
        async with self._lock:
            for ws in self._connections:
                try:
                    await ws.send_text(message)
                except Exception:
                    disconnected.append(ws)

        if disconnected:
            async with self._lock:
                for ws in disconnected:
                    if ws in self._connections:
                        self._connections.remove(ws)

    @property
    def client_count(self) -> int:
        """Number of active WebSocket connections."""
        return len(self._connections)


# Singleton instance
manager = ConnectionManager()
