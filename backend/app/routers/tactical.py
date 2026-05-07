"""
Mesh Pi5 Server — Tactical Graphics API Router
"""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel
from typing import List

from app.database import get_db
from app.models.tactical import TacticalGraphicEntity

router = APIRouter()

class TacticalGraphicCreate(BaseModel):
    graphic_type: str
    name: str
    color: str
    geojson_data: str

class TacticalGraphicResponse(TacticalGraphicCreate):
    id: int

    class Config:
        from_attributes = True

@router.get("/", response_model=List[TacticalGraphicResponse])
async def get_tactical_graphics(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(TacticalGraphicEntity))
    return result.scalars().all()

@router.post("/", response_model=TacticalGraphicResponse)
async def create_tactical_graphic(graphic: TacticalGraphicCreate, db: AsyncSession = Depends(get_db)):
    entity = TacticalGraphicEntity(
        graphic_type=graphic.graphic_type,
        name=graphic.name,
        color=graphic.color,
        geojson_data=graphic.geojson_data
    )
    db.add(entity)
    await db.commit()
    await db.refresh(entity)
    return entity

@router.delete("/{graphic_id}")
async def delete_tactical_graphic(graphic_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(TacticalGraphicEntity).where(TacticalGraphicEntity.id == graphic_id))
    entity = result.scalar_one_or_none()
    if not entity:
        raise HTTPException(status_code=404, detail="Graphic not found")
    
    await db.delete(entity)
    await db.commit()
    return {"status": "success"}
