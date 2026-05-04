"""
Mesh Pi5 Server — Events API Router

File: routers/events.py
Description: REST endpoints for system event log.
"""

from fastapi import APIRouter, Depends, Query
from sqlalchemy import select, desc
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import EventEntity

router = APIRouter()


@router.get("/")
async def list_events(
    severity: str | None = Query(None, description="Filter by severity: INFO, WARNING, CRITICAL"),
    node_id: int | None = Query(None, description="Filter by node ID"),
    limit: int = Query(50, ge=1, le=500),
    db: AsyncSession = Depends(get_db),
):
    """Get system events with optional filters."""
    query = select(EventEntity).order_by(desc(EventEntity.created_at)).limit(limit)

    if severity:
        query = query.where(EventEntity.severity == severity.upper())
    if node_id is not None:
        query = query.where(EventEntity.node_id == node_id)

    result = await db.execute(query)
    events = result.scalars().all()

    return [
        {
            "id": e.id,
            "node_id": e.node_id,
            "event_type": e.event_type,
            "severity": e.severity,
            "message": e.message,
            "data": e.data_json,
            "created_at": e.created_at.isoformat() if e.created_at else None,
        }
        for e in events
    ]
