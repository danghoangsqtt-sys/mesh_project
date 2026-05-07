"""
Mesh Pi5 Server — AI LLM Service

File: services/llm_service.py
Description: Interfaces with local LLM (Ollama) to parse natural language tactical commands.
"""

import httpx
import re
import json
from typing import Dict, Any, Optional

OLLAMA_URL = "http://127.0.0.1:11434/api/generate"
DEFAULT_MODEL = "phi3" # or "llama3"

class LLMService:
    async def process_tactical_command(self, prompt: str) -> Dict[str, Any]:
        """
        Parses a natural language prompt into a structured command action.
        Uses local Ollama if available, otherwise returns fallback.
        """
        system_prompt = """
You are a tactical military AI assistant. 
Your job is to parse the user's natural language command into a structured JSON response.
Supported actions: 'BROADCAST_MSG', 'SEND_SOS', 'PING_NODE'.
Output ONLY valid JSON.
Example format:
{"action": "BROADCAST_MSG", "payload": "Fall back to base", "target": "ALL"}
"""
        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                response = await client.post(OLLAMA_URL, json={
                    "model": DEFAULT_MODEL,
                    "prompt": system_prompt + "\nUser: " + prompt + "\nJSON:",
                    "stream": False,
                    "format": "json"
                })
                
                if response.status_code == 200:
                    result = response.json()
                    # Parse the JSON string returned by the model
                    parsed = json.loads(result.get("response", "{}"))
                    return {
                        "status": "success", 
                        "action": parsed.get("action", "UNKNOWN"),
                        "payload": parsed.get("payload", ""),
                        "target": parsed.get("target", "ALL"),
                        "raw": result.get("response")
                    }
        except Exception as e:
            print(f"Ollama connection failed: {e}")
            
        # Fallback keyword-based parser if AI is offline
        prompt_lower = prompt.lower()
        if "sos" in prompt_lower or "khẩn cấp" in prompt_lower:
            return {"status": "fallback", "action": "SEND_SOS", "payload": "Emergency", "target": "ALL"}
        elif "gửi" in prompt_lower or "thông báo" in prompt_lower:
            return {"status": "fallback", "action": "BROADCAST_MSG", "payload": prompt, "target": "ALL"}
            
        return {
            "status": "error", 
            "message": "AI is offline and command not understood.",
            "action": "UNKNOWN"
        }

llm_service = LLMService()
