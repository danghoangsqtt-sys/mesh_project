from sqlalchemy import Column, Integer, String, Text, DateTime, func
from app.database import Base

class TacticalGraphicEntity(Base):
    """Database model for Tactical Graphics (POIs, Zones)."""

    __tablename__ = "tactical_graphics"

    id = Column(Integer, primary_key=True, index=True)
    graphic_type = Column(String(50), nullable=False) # 'point', 'polygon'
    name = Column(String(100), nullable=False) # 'LZ', 'Base', 'Safe Zone'
    color = Column(String(20), nullable=False) # '#10b981', '#ef4444'
    geojson_data = Column(Text, nullable=False) # JSON string of GeoJSON geometry
    created_at = Column(DateTime(timezone=True), server_default=func.now())
