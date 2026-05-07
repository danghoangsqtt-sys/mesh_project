"""
Mesh Pi5 Server — Geofencing Service

File: services/geofencing.py
Description: Evaluates point-in-polygon for nodes to trigger zone alerts.
             Tracks three zone types: safe, shared (neutral), danger.
"""

from typing import Optional
from shapely.geometry import Point, Polygon
from app.models import EventEntity


# Zone definitions mirror the frontend ZONES constant in TacticalMap.tsx
ZONE_DEFINITIONS = {
    "safe": {
        "coords": [
            (109.1920, 12.2370),
            (109.1950, 12.2370),
            (109.1950, 12.2400),
            (109.1920, 12.2400),
        ],
        "label": "Vùng An Toàn",
        "enter_severity": "INFO",
        "exit_severity": "INFO",
        "enter_type": "SAFE_ZONE_ENTER",
        "exit_type": "SAFE_ZONE_EXIT",
    },
    "shared": {
        "coords": [
            (109.1950, 12.2370),
            (109.1980, 12.2370),
            (109.1980, 12.2400),
            (109.1950, 12.2400),
        ],
        "label": "Vùng Chung",
        "enter_severity": "INFO",
        "exit_severity": "INFO",
        "enter_type": "SHARED_ZONE_ENTER",
        "exit_type": "SHARED_ZONE_EXIT",
    },
    "danger": {
        "coords": [
            (109.1980, 12.2370),
            (109.2010, 12.2370),
            (109.2010, 12.2400),
            (109.1980, 12.2400),
        ],
        "label": "Vùng Nguy Hiểm",
        "enter_severity": "CRITICAL",
        "exit_severity": "INFO",
        "enter_type": "GEOFENCE_BREACH",
        "exit_type": "GEOFENCE_EXIT",
    },
}


class GeofencingService:
    def __init__(self):
        self.zones = {
            zone_id: Polygon(defn["coords"])
            for zone_id, defn in ZONE_DEFINITIONS.items()
        }
        # node_id -> set of zone_ids the node is currently inside
        self._node_zone_state: dict[int, set[str]] = {}

    def check_node(self, node_id: int, lat: float, lng: float) -> list[EventEntity]:
        """Check zone boundary crossings for a node.
        Returns a list of EventEntity for each enter/exit event detected.
        """
        pt = Point(lng, lat)
        prev_zones = self._node_zone_state.get(node_id, set())
        curr_zones = {zid for zid, poly in self.zones.items() if poly.contains(pt)}

        entered = curr_zones - prev_zones
        exited = prev_zones - curr_zones
        self._node_zone_state[node_id] = curr_zones

        events: list[EventEntity] = []
        for zone_id in entered:
            defn = ZONE_DEFINITIONS[zone_id]
            events.append(EventEntity(
                node_id=node_id,
                event_type=defn["enter_type"],
                severity=defn["enter_severity"],
                message=f"Node {node_id} entered {defn['label']}.",
            ))
        for zone_id in exited:
            defn = ZONE_DEFINITIONS[zone_id]
            events.append(EventEntity(
                node_id=node_id,
                event_type=defn["exit_type"],
                severity=defn["exit_severity"],
                message=f"Node {node_id} exited {defn['label']}.",
            ))

        return events

    def get_zone_for_node(self, node_id: int) -> Optional[str]:
        """Return the most critical zone a node is currently in, or None."""
        current = self._node_zone_state.get(node_id, set())
        for priority in ("danger", "shared", "safe"):
            if priority in current:
                return priority
        return None


geofencing_service = GeofencingService()
