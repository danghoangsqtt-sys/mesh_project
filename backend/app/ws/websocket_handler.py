"""
Mesh Pi5 Server — WebSocket endpoint

File: ws/websocket_handler.py
Description: WebSocket endpoint that streams real-time mesh data to browser clients.
"""

import asyncio
import logging

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app.services.serial_bridge import serial_bridge
from app.ws.connection_manager import manager

logger = logging.getLogger(__name__)

router = APIRouter()


from app.services.geofencing import geofencing_service
from app.services.vitals import vitals_service
from app.services.ai_health_analyzer import ai_health_analyzer
from app.database import async_session

async def _packet_broadcaster():
    """Background task: consume packets from serial bridge queue and broadcast."""
    while True:
        try:
            packet = await serial_bridge.packet_queue.get()
            
            # Check geofence (returns list of zone enter/exit events)
            geofence_events = geofencing_service.check_node(packet.node_id, packet.latitude, packet.longitude)

            # Check vitals
            vital_events = vitals_service.check_vitals(
                packet.node_id, packet.heart_rate, packet.spo2,
                packet.battery_voltage, packet.latitude, packet.longitude
            )
            
            # AI Health Prediction (Phase 1)
            ai_events = await ai_health_analyzer.analyze_node(
                packet.node_id, packet.heart_rate, packet.spo2, packet.temperature
            )

            all_events = vital_events + geofence_events + ai_events
            
            # Broadcast the packet
            await manager.broadcast_packet(packet)
            
            # Save node state, history, and events
            from sqlalchemy import select
            from app.models.soldier import SoldierEntity, PositionHistoryEntity
            from datetime import datetime
            import math
            
            def sanitize_float(val):
                return 0.0 if math.isnan(val) else val
            
            lat_s = sanitize_float(packet.latitude)
            lon_s = sanitize_float(packet.longitude)
            hdg_s = sanitize_float(packet.heading)
            temp_s = sanitize_float(packet.temperature)
            hum_s = sanitize_float(packet.humidity)
            pres_s = sanitize_float(packet.pressure)
            batt_s = sanitize_float(packet.battery_voltage)
            
            async with async_session() as db:
                # Upsert Soldier
                result = await db.execute(select(SoldierEntity).where(SoldierEntity.node_id == packet.node_id))
                soldier = result.scalar_one_or_none()
                if not soldier:
                    soldier = SoldierEntity(node_id=packet.node_id)
                    db.add(soldier)
                
                soldier.latitude = lat_s
                soldier.longitude = lon_s
                soldier.heading = hdg_s
                soldier.heart_rate = packet.heart_rate
                soldier.spo2 = packet.spo2
                soldier.temperature = temp_s
                soldier.battery_voltage = batt_s
                soldier.status_flags = packet.status_flags
                soldier.last_seen = datetime.now()
                
                # Add position history
                history = PositionHistoryEntity(
                    node_id=packet.node_id,
                    latitude=lat_s,
                    longitude=lon_s,
                    heading=hdg_s,
                    timestamp=packet.timestamp
                )
                db.add(history)

                if all_events:
                    for event in all_events:
                        db.add(event)
                await db.commit()
                
                for event in all_events:
                    await manager.broadcast_event("EVENT", {
                        "node_id": event.node_id,
                        "event_type": event.event_type,
                        "severity": event.severity,
                        "message": event.message
                    })
        except asyncio.CancelledError:
            break
        except Exception as e:
            logger.error("Error broadcasting packet: %s", e)


async def _timeout_checker():
    """Background task: periodically check for disconnected nodes."""
    while True:
        try:
            await asyncio.sleep(10)
            timeout_events = vitals_service.check_timeouts()
            if timeout_events:
                async with async_session() as db:
                    for event in timeout_events:
                        db.add(event)
                    await db.commit()
                
                for event in timeout_events:
                    await manager.broadcast_event("EVENT", {
                        "node_id": event.node_id,
                        "event_type": event.event_type,
                        "severity": event.severity,
                        "message": event.message
                    })
        except asyncio.CancelledError:
            break
        except Exception as e:
            logger.error("Error checking timeouts: %s", e)

# Will be started as a background task when first client connects
_broadcaster_task: asyncio.Task | None = None
_timeout_task: asyncio.Task | None = None

def _ensure_broadcaster():
    """Ensure the packet broadcaster background task is running."""
    global _broadcaster_task, _timeout_task
    if _broadcaster_task is None or _broadcaster_task.done():
        _broadcaster_task = asyncio.create_task(_packet_broadcaster())
    if _timeout_task is None or _timeout_task.done():
        _timeout_task = asyncio.create_task(_timeout_checker())


@router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """WebSocket endpoint for real-time mesh data streaming."""
    await manager.connect(websocket)
    _ensure_broadcaster()

    try:
        while True:
            # Keep connection alive; handle incoming messages from client
            data = await websocket.receive_text()
            # Future: handle client commands via WebSocket
            logger.debug("Received from client: %s", data)
    except WebSocketDisconnect:
        await manager.disconnect(websocket)
    except Exception as e:
        logger.error("WebSocket error: %s", e)
        await manager.disconnect(websocket)
