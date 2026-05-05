"""
Mesh Pi5 Server — Vision Integration Stub

File: services/vision.py
Description: Stub for future YOLO object detection integration.
"""

import asyncio
import random
from typing import List, Dict

class VisionService:
    def __init__(self):
        self.is_active = False
        self._targets: List[Dict] = []
        
    async def run_inference_loop(self):
        """Mock inference loop that generates random target data."""
        self.is_active = True
        while self.is_active:
            await asyncio.sleep(5) # Run every 5 seconds
            
            # Simulate detecting a target (e.g. enemy vehicle) near center
            # Base coords: 12.2388, 109.1967
            if random.random() > 0.5:
                lat = 12.2388 + (random.random() - 0.5) * 0.005
                lng = 109.1967 + (random.random() - 0.5) * 0.005
                
                target = {
                    "id": f"tgt_{random.randint(1000,9999)}",
                    "class": random.choice(["person", "vehicle"]),
                    "confidence": round(random.uniform(0.6, 0.95), 2),
                    "latitude": lat,
                    "longitude": lng
                }
                
                # Keep last 5 targets
                self._targets.append(target)
                if len(self._targets) > 5:
                    self._targets.pop(0)

    def get_targets(self) -> List[Dict]:
        return self._targets
        
    def start(self):
        asyncio.create_task(self.run_inference_loop())
        
vision_service = VisionService()
