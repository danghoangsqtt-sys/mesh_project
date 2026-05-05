"""
Mesh Pi5 Server — SQLAlchemy Models

File: models/soldier.py
Description: SoldierEntity — represents a mesh node (soldier) with GPS, vitals, and status.
"""

from datetime import datetime

from sqlalchemy import Integer, Float, Text, DateTime, func
from sqlalchemy.orm import Mapped, mapped_column

from app.database import Base


class SoldierEntity(Base):
    """A soldier/node in the LoRa mesh network."""

    __tablename__ = "soldiers"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    node_id: Mapped[int] = mapped_column(Integer, unique=True, nullable=False)
    name: Mapped[str | None] = mapped_column(Text, default=None)
    latitude: Mapped[float] = mapped_column(Float, default=0.0)
    longitude: Mapped[float] = mapped_column(Float, default=0.0)
    heading: Mapped[float] = mapped_column(Float, default=0.0)
    heart_rate: Mapped[int] = mapped_column(Integer, default=0)
    spo2: Mapped[int] = mapped_column(Integer, default=0)
    temperature: Mapped[float] = mapped_column(Float, default=0.0)
    humidity: Mapped[float] = mapped_column(Float, default=0.0)
    pressure: Mapped[float] = mapped_column(Float, default=0.0)
    battery_voltage: Mapped[float] = mapped_column(Float, default=0.0)
    status_flags: Mapped[int] = mapped_column(Integer, default=0)
    last_seen: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), onupdate=func.now()
    )


class PositionHistoryEntity(Base):
    """Historical position record for path trail rendering."""

    __tablename__ = "position_history"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    node_id: Mapped[int] = mapped_column(Integer, nullable=False, index=True)
    latitude: Mapped[float] = mapped_column(Float, nullable=False)
    longitude: Mapped[float] = mapped_column(Float, nullable=False)
    heading: Mapped[float] = mapped_column(Float, default=0.0)
    timestamp: Mapped[int] = mapped_column(Integer, nullable=False)
    recorded_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class EventEntity(Base):
    """System event log entry (SOS, alerts, geofence breaches)."""

    __tablename__ = "events"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    node_id: Mapped[int | None] = mapped_column(Integer, default=None, index=True)
    event_type: Mapped[str] = mapped_column(Text, nullable=False)
    severity: Mapped[str] = mapped_column(Text, default="INFO")
    message: Mapped[str] = mapped_column(Text, nullable=False)
    data_json: Mapped[str | None] = mapped_column(Text, default=None)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class CommandEntity(Base):
    """Command sent to a node via gateway."""

    __tablename__ = "commands"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    target_node_id: Mapped[int | None] = mapped_column(Integer, default=None)
    command_type: Mapped[str] = mapped_column(Text, nullable=False)
    payload: Mapped[bytes | None] = mapped_column(default=None)
    status: Mapped[str] = mapped_column(Text, default="PENDING")
    retry_count: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    sent_at: Mapped[datetime | None] = mapped_column(DateTime, default=None)
    acked_at: Mapped[datetime | None] = mapped_column(DateTime, default=None)
