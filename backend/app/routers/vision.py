"""
Mesh Pi5 Server — Vision API Router

File: routers/vision.py
"""

from fastapi import APIRouter
from app.services.vision import vision_service

router = APIRouter()

@router.get("/targets")
async def get_detected_targets():
    """Retrieve list of currently detected targets from the vision service."""
    return {"targets": vision_service.get_targets()}
