"""
Mesh Pi5 Server — Vitals Monitoring Service

File: services/vitals.py
Description: Evaluates incoming node vitals for critical conditions and handles connectivity timeouts.
"""

import time
from typing import Dict, List
from app.models import EventEntity

class VitalsService:
    def __init__(self):
        self.last_seen: Dict[int, float] = {}
        self.node_locations: Dict[int, tuple] = {}
        self.active_alerts: Dict[int, set] = {}

    def check_vitals(self, node_id: int, heart_rate: int, spo2: int, battery: float, lat: float, lng: float) -> List[EventEntity]:
        """Check vitals and return list of generated alert events."""
        events = []
        now = time.time()
        self.last_seen[node_id] = now
        self.node_locations[node_id] = (lat, lng)
        
        if node_id not in self.active_alerts:
            self.active_alerts[node_id] = set()
            
        alerts = self.active_alerts[node_id]

        # Check Heart Rate
        if heart_rate > 0 and heart_rate < 60:
            if "HR_LOW" not in alerts:
                events.append(EventEntity(
                    node_id=node_id, event_type="alert_hr_low", severity="CRITICAL",
                    message="instr_check_medic"
                ))
                alerts.add("HR_LOW")
        elif "HR_LOW" in alerts and heart_rate >= 60:
            alerts.remove("HR_LOW")

        # Check SpO2
        if spo2 > 0 and spo2 < 90:
            if "SPO2_LOW" not in alerts:
                events.append(EventEntity(
                    node_id=node_id, event_type="alert_spo2_low", severity="CRITICAL",
                    message="instr_check_medic"
                ))
                alerts.add("SPO2_LOW")
        elif "SPO2_LOW" in alerts and spo2 >= 90:
            alerts.remove("SPO2_LOW")
            
        # Check Battery
        if battery < 3.3:
            if "POWER_LOSS" not in alerts:
                events.append(EventEntity(
                    node_id=node_id, event_type="alert_power_loss", severity="WARNING",
                    message="instr_replace_bat"
                ))
                alerts.add("POWER_LOSS")
        elif "POWER_LOSS" in alerts and battery >= 3.3:
            alerts.remove("POWER_LOSS")

        return events
        
    def check_timeouts(self) -> List[EventEntity]:
        """Check for nodes that haven't sent data in 30 seconds."""
        events = []
        now = time.time()
        for node_id, last_time in list(self.last_seen.items()):
            if now - last_time > 30:
                if node_id not in self.active_alerts:
                    self.active_alerts[node_id] = set()
                if "CONN_LOST" not in self.active_alerts[node_id]:
                    lat, lng = self.node_locations.get(node_id, (0, 0))
                    events.append(EventEntity(
                        node_id=node_id, event_type="alert_conn_lost", severity="WARNING",
                        message="instr_check_conn"
                    ))
                    self.active_alerts[node_id].add("CONN_LOST")
            elif "CONN_LOST" in self.active_alerts.get(node_id, set()):
                self.active_alerts[node_id].remove("CONN_LOST")
        return events

vitals_service = VitalsService()
