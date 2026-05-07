"""
Mesh Pi5 Server — AI Pathfinding Service

File: services/pathfinding.py
Description: Dynamic Grid A* algorithm for routing on tactical map avoiding danger zones.
"""

import math
import networkx as nx
from typing import List, Tuple

class PathfindingService:
    def __init__(self):
        self.danger_zones = []

    def _haversine(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        R = 6371000
        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)
        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)
        a = math.sin(delta_phi / 2.0) ** 2 + \
            math.cos(phi1) * math.cos(phi2) * \
            math.sin(delta_lambda / 2.0) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        return R * c

    def set_danger_zones(self, zones: List[dict]):
        self.danger_zones = zones

    def _get_danger_penalty(self, lat: float, lng: float) -> float:
        penalty = 0.0
        for zone in self.danger_zones:
            dist = self._haversine(lat, lng, zone["lat"], zone["lng"])
            if dist <= zone["radius"]:
                penalty += 1000000.0 * (1.0 - (dist / max(zone["radius"], 1.0))) + 10000.0
        return penalty

    def find_shortest_path(self, start_lat: float, start_lng: float, end_lat: float, end_lng: float) -> List[Tuple[float, float]]:
        min_lat = min(start_lat, end_lat)
        max_lat = max(start_lat, end_lat)
        min_lng = min(start_lng, end_lng)
        max_lng = max(start_lng, end_lng)

        lat_diff = max(max_lat - min_lat, 0.001)
        lng_diff = max(max_lng - min_lng, 0.001)
        pad_lat = lat_diff * 0.6
        pad_lng = lng_diff * 0.6
        min_lat -= pad_lat
        max_lat += pad_lat
        min_lng -= pad_lng
        max_lng += pad_lng

        GRID = 40
        step_lat = (max_lat - min_lat) / GRID
        step_lng = (max_lng - min_lng) / GRID

        graph = nx.Graph()
        for i in range(GRID + 1):
            for j in range(GRID + 1):
                lat = min_lat + i * step_lat
                lng = min_lng + j * step_lng
                graph.add_node((i, j), pos=(lat, lng))

        for i in range(GRID + 1):
            for j in range(GRID + 1):
                for ni, nj in [(i+1, j), (i, j+1), (i+1, j+1), (i-1, j+1)]:
                    if 0 <= ni <= GRID and 0 <= nj <= GRID:
                        p1 = graph.nodes[(i, j)]['pos']
                        p2 = graph.nodes[(ni, nj)]['pos']
                        dist = self._haversine(p1[0], p1[1], p2[0], p2[1])
                        mid_lat = (p1[0] + p2[0]) / 2.0
                        mid_lng = (p1[1] + p2[1]) / 2.0
                        penalty = self._get_danger_penalty(mid_lat, mid_lng)
                        graph.add_edge((i, j), (ni, nj), weight=dist + penalty)

        def get_nearest(lat, lng):
            i = max(0, min(GRID, int(round((lat - min_lat) / step_lat))))
            j = max(0, min(GRID, int(round((lng - min_lng) / step_lng))))
            return (i, j)

        start_node = get_nearest(start_lat, start_lng)
        end_node = get_nearest(end_lat, end_lng)

        if start_node == end_node:
            return [(start_lat, start_lng), (end_lat, end_lng)]

        def heuristic(u, v):
            p1 = graph.nodes[u]['pos']
            p2 = graph.nodes[v]['pos']
            return self._haversine(p1[0], p1[1], p2[0], p2[1])

        try:
            path = nx.astar_path(graph, start_node, end_node, heuristic=heuristic, weight='weight')
            coords = [graph.nodes[n]['pos'] for n in path]
            coords[0] = (start_lat, start_lng)
            coords[-1] = (end_lat, end_lng)
            return coords
        except nx.NetworkXNoPath:
            return []

pathfinding_service = PathfindingService()
