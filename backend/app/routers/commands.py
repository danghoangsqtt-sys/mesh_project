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
    from app.services.packet_parser import build_command_packet, build_frame
    from app.services.serial_bridge import serial_bridge

    # Map string command_type to int
    cmd_map = {"CONFIG": 0x01, "OTA": 0x02, "BROADCAST": 0x03}
    cmd_type_int = cmd_map.get(cmd.command_type.upper(), 0x00)

    payload = bytes.fromhex(cmd.payload_hex) if cmd.payload_hex else b""
    target_id = cmd.target_node_id if cmd.target_node_id is not None else 0xFFFF

    # Build and frame the packet
    packet_bytes = build_command_packet(target_id, cmd_type_int, payload)
    frame_bytes = build_frame(packet_bytes)

    # Save to database
    entity = CommandEntity(
        target_node_id=cmd.target_node_id,
        command_type=cmd.command_type,
        payload=payload,
        status="PENDING",
    )
    db.add(entity)
    await db.flush()

    # Write to serial bridge
    success = await serial_bridge.write(frame_bytes)
    
    if success:
        entity.status = "SENT"
        import datetime
        entity.sent_at = datetime.datetime.now()
        await db.commit()
    else:
        entity.status = "FAILED"
        await db.commit()
        raise HTTPException(status_code=500, detail="Failed to write command to serial port.")

    return {
        "id": entity.id,
        "status": entity.status,
        "message": f"Command {cmd.command_type} sent to node {cmd.target_node_id or 'BROADCAST'}",
    }
