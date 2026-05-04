"""
Mesh Pi5 Server — AI & Pathfinding API Router

File: routers/ai.py
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Tuple
from app.services.pathfinding import pathfinding_service

router = APIRouter()

class PathRequest(BaseModel):
    start_lat: float
    start_lng: float
    end_lat: float
    end_lng: float

@router.post("/pathfinding")
async def calculate_path(req: PathRequest):
    """Calculate the shortest path between two coordinates using A*."""
    path_coords = pathfinding_service.find_shortest_path(
        req.start_lat, req.start_lng,
        req.end_lat, req.end_lng
    )
    
    if not path_coords:
        raise HTTPException(status_code=404, detail="No path found between locations")
        
    # Convert to GeoJSON LineString
    geojson = {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [[lng, lat] for lat, lng in path_coords] # GeoJSON uses [lng, lat]
        },
        "properties": {
            "distance_meters": 0 # Would calculate actual distance here
        }
    }
    
    return geojson
