"""
Mesh Pi5 Server — AI Health Predictor (Phase 1 & Scaffolding)

File: services/ai_health_analyzer.py
Description: Ingests time-series health data from nodes, maintains a sliding window,
             and generates rule-based predictive warnings for exhaustion and heat stress.
"""

import time
import collections
from typing import Dict, List
from datetime import datetime
from app.models import EventEntity
from app.services.llm_service import llm_service

class NodeHealthData:
    def __init__(self, max_history=60):
        # Store tuples of (timestamp, hr, spo2, temp)
        self.history = collections.deque(maxlen=max_history)
        self.last_anomaly_score = 0.0

class AIHealthAnalyzer:
    def __init__(self):
        # We specifically track nodes of interest for AI (Node 1 and Node 3)
        self.nodes_data: Dict[int, NodeHealthData] = {
            1: NodeHealthData(),
            3: NodeHealthData()
        }
        self.active_warnings: Dict[int, set] = {
            1: set(),
            3: set()
        }
        
    async def analyze_node(self, node_id: int, heart_rate: int, spo2: int, temp: float) -> List[EventEntity]:
        """Ingest new data and return any AI predictions or early warnings."""
        if node_id not in self.nodes_data:
            # We can dynamically add nodes if we want to track everyone
            self.nodes_data[node_id] = NodeHealthData()
            self.active_warnings[node_id] = set()
            
        now = time.time()
        node_data = self.nodes_data[node_id]
        
        # Only record valid readings (filter out 0s if sensors are off)
        if heart_rate > 0 or spo2 > 0:
            node_data.history.append((now, heart_rate, spo2, temp))
        
        events = []
        warnings = self.active_warnings[node_id]

        # --- SENSOR DISCONNECT CHECK ---
        if heart_rate == 0 or spo2 == 0:
            if "sensor_error" not in warnings:
                warnings.add("sensor_error")
                events.append(EventEntity(
                    id=int(now * 1000),
                    node_id=node_id,
                    event_type="sensor_error",
                    severity="WARNING",
                    message="Lỗi cảm biến: Mất tín hiệu nhịp tim hoặc SpO2.",
                    created_at=datetime.utcnow().isoformat() + "Z"
                ))
            return events
        else:
            if "sensor_error" in warnings:
                warnings.remove("sensor_error")
        
        # --- PHASE 1: Qwen 2.5 Local AI Analysis ---
        # Instead of fixed rules, we ask Qwen 2.5 to analyze the vitals.
        # We only ping Qwen if values are somewhat abnormal to save resources
        needs_ai_check = False
        if temp > 37.5 or heart_rate > 100 or spo2 < 95 or heart_rate < 50:
            needs_ai_check = True
            
        if needs_ai_check:
            ai_result = await llm_service.analyze_vitals(node_id, heart_rate, spo2, temp)
            if ai_result:
                if "AI_WARNING" not in warnings:
                    events.append(EventEntity(
                        node_id=node_id, 
                        event_type="ai_predict_health", 
                        severity=ai_result.get("severity", "WARNING"),
                        message=f"AI Qwen: {ai_result.get('message', 'Bất thường sinh hiệu.')}"
                    ))
                    warnings.add("AI_WARNING")
            else:
                if "AI_WARNING" in warnings:
                    warnings.remove("AI_WARNING")
            
        # 2. Hypoxia / Exhaustion Trend (Requires history)
        if len(node_data.history) >= 10:
            # Check if SpO2 has been consistently <= 93% for the last 10 readings
            recent_spo2 = [data[2] for data in list(node_data.history)[-10:]]
            if all(s > 0 and s <= 93 for s in recent_spo2):
                if "HYPOXIA_TREND" not in warnings:
                    events.append(EventEntity(
                        node_id=node_id, 
                        event_type="ai_predict_hypoxia", 
                        severity="WARNING",
                        message="Dự báo thiếu oxy: SpO2 liên tục giảm nhẹ trong thời gian gần đây."
                    ))
                    warnings.add("HYPOXIA_TREND")
            elif "HYPOXIA_TREND" in warnings and recent_spo2[-1] >= 95:
                warnings.remove("HYPOXIA_TREND")
                
        # --- PHASE 2 PREP: Anomaly Scoring ---
        # Calculate a basic anomaly score based on HR volatility
        if len(node_data.history) >= 5:
            recent_hr = [data[1] for data in list(node_data.history)[-5:] if data[1] > 0]
            if recent_hr:
                avg_hr = sum(recent_hr) / len(recent_hr)
                variance = sum((h - avg_hr) ** 2 for h in recent_hr) / len(recent_hr)
                # High variance in HR indicates erratic physical stress
                node_data.last_anomaly_score = min(1.0, variance / 500.0) 
            
        return events

ai_health_analyzer = AIHealthAnalyzer()
