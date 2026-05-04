"""
Mesh Pi5 Server — Web-based Command & Control for LoRa Mesh Network
Copyright (c) 2026 — All rights reserved

File: main.py
Description: FastAPI application entry point with lifespan management.
"""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import init_db
from app.routers import nodes, events, commands, status, ota, ai, vision
from app.ws.connection_manager import manager
from app.ws.websocket_handler import router as ws_router
from app.services.serial_bridge import serial_bridge
from app.services.vision import vision_service


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan: start/stop serial bridge and WebSocket manager."""
    # Startup
    await init_db()
    await serial_bridge.start()
    vision_service.start()
    yield
    # Shutdown
    await serial_bridge.stop()
    vision_service.is_active = False
    await manager.disconnect_all()


app = FastAPI(
    title="Mesh Pi5 Server",
    description="Web-based Command & Control for LoRa Mesh Network",
    version=settings.APP_VERSION,
    lifespan=lifespan,
)

# CORS — allow all origins since we're on a local WiFi AP
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(nodes.router, prefix="/api/nodes", tags=["nodes"])
app.include_router(events.router, prefix="/api/events", tags=["events"])
app.include_router(commands.router, prefix="/api/commands", tags=["commands"])
app.include_router(status.router, prefix="/api/status", tags=["status"])
app.include_router(ota.router, prefix="/api/ota", tags=["ota"])
app.include_router(ai.router, prefix="/api/ai", tags=["ai"])
app.include_router(vision.router, prefix="/api/vision", tags=["vision"])
app.include_router(ws_router)


@app.get("/")
async def root():
    """Health check endpoint."""
    return {"status": "ok", "service": "mesh-pi5-server", "version": settings.APP_VERSION}
