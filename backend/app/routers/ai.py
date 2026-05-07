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

class DangerZone(BaseModel):
    lat: float
    lng: float
    radius: float

@router.get("/danger-zones")
async def get_danger_zones():
    """Get list of current danger zones."""
    return {"danger_zones": pathfinding_service.danger_zones}

@router.post("/danger-zones")
async def update_danger_zones(zones: List[DangerZone]):
    """Update the list of active danger zones."""
    zone_dicts = [{"lat": z.lat, "lng": z.lng, "radius": z.radius} for z in zones]
    pathfinding_service.set_danger_zones(zone_dicts)
    return {"message": "Danger zones updated successfully", "count": len(zone_dicts)}


@router.get("/zones")
async def get_all_zones():
    """Return all tactical zone polygons as a GeoJSON FeatureCollection.

    Each feature carries zone metadata (type, label, color, icon) so the
    frontend can dynamically render them without hard-coding coordinates.
    """
    from app.services.geofencing import ZONE_DEFINITIONS

    ZONE_STYLE = {
        "safe":   {"color": "#10b981", "fillOpacity": 0.18, "icon": "✓"},
        "shared": {"color": "#f59e0b", "fillOpacity": 0.15, "icon": "◎"},
        "danger": {"color": "#ef4444", "fillOpacity": 0.22, "icon": "⚠"},
    }

    features = []
    for zone_id, defn in ZONE_DEFINITIONS.items():
        style = ZONE_STYLE[zone_id]
        coords = [list(c) for c in defn["coords"]]
        coords.append(coords[0])  # close polygon ring
        features.append({
            "type": "Feature",
            "properties": {
                "zone_id": zone_id,
                "label": defn["label"],
                "color": style["color"],
                "fillOpacity": style["fillOpacity"],
                "icon": style["icon"],
            },
            "geometry": {
                "type": "Polygon",
                "coordinates": [coords],
            },
        })

    return {"type": "FeatureCollection", "features": features}
