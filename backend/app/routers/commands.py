"""
Mesh Pi5 Server — Commands API Router

File: routers/commands.py
Description: REST endpoints for sending commands to nodes via Gateway.
"""

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import CommandEntity

router = APIRouter()


class CommandRequest(BaseModel):
    """Request body for sending a command to a node."""
    target_node_id: int | None = None  # None = broadcast
    command_type: str  # CONFIG, OTA, BROADCAST, etc.
    payload_hex: str | None = None  # Hex-encoded payload


@router.post("/")
async def send_command(
    cmd: CommandRequest,
    db: AsyncSession = Depends(get_db),
):
    """Queue a command to be sent to a node via the Gateway."""
    payload = bytes.fromhex(cmd.payload_hex) if cmd.payload_hex else None

    entity = CommandEntity(
        target_node_id=cmd.target_node_id,
        command_type=cmd.command_type,
        payload=payload,
        status="PENDING",
    )
    db.add(entity)
    await db.flush()

    return {
        "id": entity.id,
        "status": "PENDING",
        "message": f"Command {cmd.command_type} queued for node {cmd.target_node_id or 'BROADCAST'}",
    }
