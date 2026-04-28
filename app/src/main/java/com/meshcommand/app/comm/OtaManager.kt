package com.meshcommand.app.comm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Handles slicing a firmware binary and sending it via LoRa chunks.
 */
class OtaManager {
    
    // Returns a Flow of CommandPackets with delays between them to avoid congesting the LoRa network
    fun startOtaUpload(targetNodeId: Int, firmware: ByteArray): Flow<CommandPacket> = flow {
        // 1. Send OTA START (0xFE, followed by 4 bytes size)
        val startPayload = ByteArray(27)
        startPayload[0] = 0xFE.toByte()
        val sizeBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        sizeBuffer.putInt(firmware.size)
        System.arraycopy(sizeBuffer.array(), 0, startPayload, 1, 4)
        
        yieldPacket(targetNodeId, startPayload)
        
        // Give node time to erase flash
        delay(2000)
        
        // 2. Send Chunks (0xFC, followed by 2 bytes index, 24 bytes data)
        val chunkSize = 24
        val totalChunks = (firmware.size + chunkSize - 1) / chunkSize
        
        for (i in 0 until totalChunks) {
            val chunkPayload = ByteArray(27)
            chunkPayload[0] = 0xFC.toByte()
            val idxBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
            idxBuffer.putShort(i.toShort())
            System.arraycopy(idxBuffer.array(), 0, chunkPayload, 1, 2)
            
            val offset = i * chunkSize
            val length = minOf(chunkSize, firmware.size - offset)
            System.arraycopy(firmware, offset, chunkPayload, 3, length)
            
            yieldPacket(targetNodeId, chunkPayload)
            
            // Artificial delay to prevent buffer overflow on Gateway / LoRa Tx time
            delay(150)
        }
        
        // 3. Send OTA END (0xFD)
        val endPayload = ByteArray(27)
        endPayload[0] = 0xFD.toByte()
        yieldPacket(targetNodeId, endPayload)
    }
    
    private suspend inline fun kotlinx.coroutines.flow.FlowCollector<CommandPacket>.yieldPacket(
        targetNodeId: Int, 
        payload: ByteArray
    ) {
        emit(CommandPacket(targetNodeId = targetNodeId, payloadBytes = payload))
    }
}
