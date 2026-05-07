import time
import math
from typing import Dict, List, Optional
from packet_handler import SoldierPacket

class SoldierState:
    def __init__(self, node_id: int):
        self.node_id = node_id
        self.latest_packet: Optional[SoldierPacket] = None
        self.position_history: List[Dict] = []
        self.last_update_time = 0
        self.max_history = 1000
    
    def update(self, packet: SoldierPacket):
        self.latest_packet = packet
        self.last_update_time = time.time()
        
        position = {
            'timestamp': packet.timestamp,
            'latitude': packet.latitude,
            'longitude': packet.longitude,
            'heading': packet.heading,
            'time': self.last_update_time
        }
        
        self.position_history.append(position)
        
        if len(self.position_history) > self.max_history:
            self.position_history = self.position_history[-self.max_history:]
    
    def get_kml_path(self) -> str:
        if not self.position_history:
            return ""
        
        coords = []
        for pos in self.position_history:
            coords.append(f"{pos['longitude']},{pos['latitude']},0")
        
        coord_string = " ".join(coords)
        
        kml = f"""<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
  <Document>
    <name>Soldier {self.node_id} Path</name>
    <Style id="pathStyle">
      <LineStyle>
        <color>ff0000ff</color>
        <width>3</width>
      </LineStyle>
    </Style>
    <Placemark>
      <name>Path</name>
      <styleUrl>#pathStyle</styleUrl>
      <LineString>
        <tessellate>1</tessellate>
        <coordinates>
{coord_string}
        </coordinates>
      </LineString>
    </Placemark>
  </Document>
</kml>"""
        return kml
    
    def to_dict(self) -> Dict:
        if not self.latest_packet:
            return {'nodeId': self.node_id, 'online': False}
        
        return {
            'nodeId': self.node_id,
            'online': (time.time() - self.last_update_time) < 10,
            'lastUpdate': self.last_update_time,
            **self.latest_packet.to_dict()
        }

class SoldierManager:
    def __init__(self):
        self.soldiers: Dict[int, SoldierState] = {}
        self.gateway_position: Optional[Dict] = None
    
    def update_soldier(self, packet: SoldierPacket):
        node_id = packet.node_id
        
        if node_id not in self.soldiers:
            self.soldiers[node_id] = SoldierState(node_id)
        
        self.soldiers[node_id].update(packet)
    
    def update_gateway_position(self, latitude: float, longitude: float):
        self.gateway_position = {
            'latitude': latitude,
            'longitude': longitude,
            'timestamp': time.time()
        }
    
    def get_soldier(self, node_id: int) -> Optional[SoldierState]:
        return self.soldiers.get(node_id)
    
    def get_all_soldiers(self) -> List[Dict]:
        return [soldier.to_dict() for soldier in self.soldiers.values()]
    
    def get_soldier_kml(self, node_id: int) -> Optional[str]:
        soldier = self.get_soldier(node_id)
        if soldier:
            return soldier.get_kml_path()
        return None
    
    def haversine_distance(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        R = 6371000
        
        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)
        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)
        
        a = math.sin(delta_phi/2)**2 + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda/2)**2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
        
        return R * c
    
    def get_nearest_neighbors(self, node_id: int, radius: float = 200.0) -> List[Dict]:
        soldier = self.get_soldier(node_id)
        if not soldier or not soldier.latest_packet:
            return []
        
        target_lat = soldier.latest_packet.latitude
        target_lon = soldier.latest_packet.longitude
        
        neighbors = []
        for other_id, other_soldier in self.soldiers.items():
            if other_id == node_id:
                continue
            
            if not other_soldier.latest_packet:
                continue
            
            distance = self.haversine_distance(
                target_lat, target_lon,
                other_soldier.latest_packet.latitude,
                other_soldier.latest_packet.longitude
            )
            
            if distance <= radius:
                neighbor_data = other_soldier.to_dict()
                neighbor_data['distance'] = distance
                neighbors.append(neighbor_data)
        
        neighbors.sort(key=lambda x: x['distance'])
        return neighbors
