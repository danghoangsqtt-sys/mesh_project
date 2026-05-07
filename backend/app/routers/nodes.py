"""
Mesh Pi5 Server — Nodes API Router

File: routers/nodes.py
Description: REST endpoints for querying soldier/node data.
"""

from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select, desc
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import SoldierEntity, PositionHistoryEntity

router = APIRouter()


@router.get("/")
async def list_nodes(db: AsyncSession = Depends(get_db)):
    """Get all active mesh nodes (seen in the last 15 seconds) with latest data."""
    from datetime import datetime, timedelta
    cutoff = datetime.now() - timedelta(seconds=15)
    
    result = await db.execute(
        select(SoldierEntity)
        .where(SoldierEntity.last_seen >= cutoff)
        .order_by(SoldierEntity.node_id)
    )
    soldiers = result.scalars().all()
    return [
        {
            "node_id": s.node_id,
            "name": s.name or f"Node-{s.node_id:04X}",
            "latitude": s.latitude,
            "longitude": s.longitude,
            "heading": s.heading,
            "heart_rate": s.heart_rate,
            "spo2": s.spo2,
            "temperature": s.temperature,
            "humidity": s.humidity,
            "pressure": s.pressure,
            "battery_voltage": s.battery_voltage,
            "status_flags": s.status_flags,
            "flags": {
                "gps_fix": bool(s.status_flags & 0x0001) if s.status_flags is not None else False,
                "imu_valid": bool(s.status_flags & 0x0002) if s.status_flags is not None else False,
                "hr_valid": bool(s.status_flags & 0x0004) if s.status_flags is not None else False,
                "spo2_valid": bool(s.status_flags & 0x0008) if s.status_flags is not None else False,
                "temp_valid": bool(s.status_flags & 0x0010) if s.status_flags is not None else False,
                "low_battery": bool(s.status_flags & 0x0020) if s.status_flags is not None else False,
                "critical_battery": bool(s.status_flags & 0x0040) if s.status_flags is not None else False,
                "sensor_error": bool(s.status_flags & 0x0080) if s.status_flags is not None else False,
                "alert": bool(s.status_flags & 0x0100) if s.status_flags is not None else False,
                "man_down": bool(s.status_flags & 0x0200) if s.status_flags is not None else False,
                "heat_stress": bool(s.status_flags & 0x0400) if s.status_flags is not None else False,
            },
            "last_seen": s.last_seen.isoformat() if s.last_seen else None,
        }
        for s in soldiers
    ]


@router.get("/{node_id}")
async def get_node(node_id: int, db: AsyncSession = Depends(get_db)):
    """Get detailed information for a specific node."""
    result = await db.execute(
        select(SoldierEntity).where(SoldierEntity.node_id == node_id)
    )
    soldier = result.scalar_one_or_none()
    if not soldier:
        raise HTTPException(status_code=404, detail=f"Node {node_id} not found")

    return {
        "node_id": soldier.node_id,
        "name": soldier.name or f"Node-{soldier.node_id:04X}",
        "latitude": soldier.latitude,
        "longitude": soldier.longitude,
        "heading": soldier.heading,
        "heart_rate": soldier.heart_rate,
        "spo2": soldier.spo2,
        "temperature": soldier.temperature,
        "humidity": soldier.humidity,
        "pressure": soldier.pressure,
        "battery_voltage": soldier.battery_voltage,
        "status_flags": soldier.status_flags,
        "last_seen": soldier.last_seen.isoformat() if soldier.last_seen else None,
        "created_at": soldier.created_at.isoformat() if soldier.created_at else None,
    }


@router.get("/{node_id}/history")
async def get_node_history(
    node_id: int,
    limit: int = 100,
    db: AsyncSession = Depends(get_db),
):
    """Get position history for a specific node (for path trail)."""
    result = await db.execute(
        select(PositionHistoryEntity)
        .where(PositionHistoryEntity.node_id == node_id)
        .order_by(desc(PositionHistoryEntity.recorded_at))
        .limit(limit)
    )
    positions = result.scalars().all()
    return [
        {
            "latitude": p.latitude,
            "longitude": p.longitude,
            "heading": p.heading,
            "timestamp": p.timestamp,
            "recorded_at": p.recorded_at.isoformat() if p.recorded_at else None,
        }
        for p in reversed(positions)  # Chronological order
    ]
