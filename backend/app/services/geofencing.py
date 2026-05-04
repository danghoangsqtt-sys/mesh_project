"""
Mesh Pi5 Server — Geofencing Service

File: services/geofencing.py
Description: Evaluates point-in-polygon for nodes to trigger geofence alerts.
"""

from typing import List, Tuple
from shapely.geometry import Point, Polygon
from app.models import EventEntity
from app.database import get_db

class GeofencingService:
    def __init__(self):
        # Mock geofence matching the frontend mock
        self.danger_zone = Polygon([
            (109.1950, 12.2370), 
            (109.1980, 12.2370),
            (109.1980, 12.2400), 
            (109.1950, 12.2400)
        ])
        # Track state to only alert on boundary crossing
        self.node_in_danger_zone = {}

    def check_node(self, node_id: int, lat: float, lng: float) -> EventEntity | None:
        """Check if node has entered or exited a geofenced area.
        Returns an EventEntity if an alert should be triggered, otherwise None.
        """
        pt = Point(lng, lat) # shapely uses x, y (lng, lat)
        in_danger = self.danger_zone.contains(pt)
        
        was_in_danger = self.node_in_danger_zone.get(node_id, False)
        
        if in_danger and not was_in_danger:
            self.node_in_danger_zone[node_id] = True
            return EventEntity(
                node_id=node_id,
                event_type="GEOFENCE_BREACH",
                severity="CRITICAL",
                message=f"Node {node_id} entered danger zone."
            )
        elif not in_danger and was_in_danger:
            self.node_in_danger_zone[node_id] = False
            return EventEntity(
                node_id=node_id,
                event_type="GEOFENCE_EXIT",
                severity="INFO",
                message=f"Node {node_id} exited danger zone."
            )
            
        return None

geofencing_service = GeofencingService()
