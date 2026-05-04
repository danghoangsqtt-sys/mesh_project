"""
Mesh Pi5 Server — AI Pathfinding Service

File: services/pathfinding.py
Description: A* algorithm implementation for routing on tactical map using networkx.
"""

import math
import networkx as nx
from typing import List, Tuple

class PathfindingService:
    def __init__(self):
        self.graph = nx.Graph()
        self._load_mock_graph()

    def _load_mock_graph(self):
        """Load a mock grid graph for demonstration.
        In production, this loads from an OSM PBF or GeoJSON road network.
        """
        # Central coordinates (Nha Trang area as default)
        base_lat, base_lng = 12.2388, 109.1967
        
        # Create a simple 10x10 grid network spanning ~1km
        for i in range(10):
            for j in range(10):
                node_id = f"node_{i}_{j}"
                lat = base_lat + (i * 0.001)
                lng = base_lng + (j * 0.001)
                self.graph.add_node(node_id, pos=(lat, lng))
                
                # Add edges to neighbors (grid)
                if i > 0:
                    self.graph.add_edge(node_id, f"node_{i-1}_{j}")
                if j > 0:
                    self.graph.add_edge(node_id, f"node_{i}_{j-1}")

    def _haversine(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        """Calculate the great circle distance between two points in meters."""
        R = 6371000 # Earth radius in meters
        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)
        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)

        a = math.sin(delta_phi / 2.0) ** 2 + \
            math.cos(phi1) * math.cos(phi2) * \
            math.sin(delta_lambda / 2.0) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

        return R * c

    def _find_nearest_node(self, lat: float, lng: float) -> str:
        """Find the nearest graph node to the given coordinates."""
        nearest = None
        min_dist = float('inf')
        
        for node, data in self.graph.nodes(data=True):
            n_lat, n_lng = data['pos']
            dist = self._haversine(lat, lng, n_lat, n_lng)
            if dist < min_dist:
                min_dist = dist
                nearest = node
                
        return nearest

    def find_shortest_path(self, start_lat: float, start_lng: float, end_lat: float, end_lng: float) -> List[Tuple[float, float]]:
        """Find shortest path using A* algorithm."""
        start_node = self._find_nearest_node(start_lat, start_lng)
        end_node = self._find_nearest_node(end_lat, end_lng)
        
        if not start_node or not end_node:
            return []
            
        def heuristic(u, v):
            u_pos = self.graph.nodes[u]['pos']
            v_pos = self.graph.nodes[v]['pos']
            return self._haversine(u_pos[0], u_pos[1], v_pos[0], v_pos[1])
            
        # Calculate weights for all edges if not present
        for u, v in self.graph.edges():
            if 'weight' not in self.graph[u][v]:
                u_pos = self.graph.nodes[u]['pos']
                v_pos = self.graph.nodes[v]['pos']
                self.graph[u][v]['weight'] = self._haversine(u_pos[0], u_pos[1], v_pos[0], v_pos[1])

        try:
            path = nx.astar_path(self.graph, start_node, end_node, heuristic=heuristic, weight='weight')
            # Convert node IDs back to coordinates
            return [self.graph.nodes[n]['pos'] for n in path]
        except nx.NetworkXNoPath:
            return []

pathfinding_service = PathfindingService()
