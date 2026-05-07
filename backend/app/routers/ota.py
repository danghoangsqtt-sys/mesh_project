"""
Mesh Pi5 Server — OTA API Router

File: routers/ota.py
Description: REST endpoints for uploading firmware and triggering OTA update over Serial.
"""

from fastapi import APIRouter, UploadFile, File, HTTPException, BackgroundTasks
import asyncio
from app.services.serial_bridge import serial_bridge
from app.services.packet_parser import build_command_packet

router = APIRouter()

async def process_ota_upload(file_content: bytes, target_node_id: int):
    """Background task to chunk and send firmware via serial."""
    # This is a simplified mock of an OTA process. 
    # In reality, this would involve sending start frame, chunks, and verifying ACKs.
    try:
        # 1. Send OTA Start command (0xFE)
        start_payload = b'\xFE' + len(file_content).to_bytes(4, "little")
        start_packet = build_command_packet(target_node_id, 0, start_payload) 
        await serial_bridge.write(start_packet)
        
        await asyncio.sleep(1) # Wait for ACK in real scenario
        
        # 2. Send chunks
        chunk_size = 128
        total_chunks = (len(file_content) + chunk_size - 1) // chunk_size
        
        for i in range(total_chunks):
            chunk = file_content[i*chunk_size : (i+1)*chunk_size]
            # Payload: 0xFC + chunk_index (2 bytes) + chunk_data
            chunk_payload = b'\xFC' + i.to_bytes(2, "little") + chunk
            chunk_packet = build_command_packet(target_node_id, 0, chunk_payload) 
            await serial_bridge.write(chunk_packet)
            await asyncio.sleep(0.1) # Rate limit / wait for ACK
            
        # 3. Send OTA End (0xFD)
        end_packet = build_command_packet(target_node_id, 0, b"\xFD") 
        await serial_bridge.write(end_packet)
        
    except Exception as e:
        import logging
        logging.getLogger(__name__).error(f"OTA Error: {e}")

@router.post("/upload/{node_id}")
async def upload_firmware(
    node_id: int,
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...)
):
    """Upload a .bin firmware file for OTA update."""
    if not file.filename.endswith(".bin"):
        raise HTTPException(status_code=400, detail="Only .bin files are allowed")
        
    content = await file.read()
    
    if len(content) > 1024 * 1024 * 2: # 2MB limit
        raise HTTPException(status_code=400, detail="Firmware file too large")
        
    background_tasks.add_task(process_ota_upload, content, node_id)
    
    return {
        "status": "success",
        "message": f"OTA update started for node {node_id}. File: {file.filename}, Size: {len(content)} bytes"
    }
