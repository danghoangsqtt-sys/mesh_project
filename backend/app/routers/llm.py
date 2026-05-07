"""
Mesh Pi5 Server — AI Assistant API Router

File: routers/llm.py
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.llm_service import llm_service

router = APIRouter()

class ChatRequest(BaseModel):
    message: str

@router.post("/chat")
async def process_chat(req: ChatRequest):
    """Process natural language command and return tactical action."""
    if not req.message.strip():
        raise HTTPException(status_code=400, detail="Message cannot be empty")
        
    result = await llm_service.process_tactical_command(req.message)
    return result
