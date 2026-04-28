package com.meshcommand.app.comm

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Represents a command message sent to a soldier node.
 * 32 bytes total.
 */
data class CommandPacket(
    val magic1: Byte = 0x5A.toByte(),
    val magic2: Byte = 0xA5.toByte(),
    val targetNodeId: Int, // 0 = broadcast
    val message: String = "",
    val payloadBytes: ByteArray? = null
) {
    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(magic1)
        buffer.put(magic2)
        buffer.put(targetNodeId.toByte())
        
        val msgBytes = payloadBytes?.take(27)?.toByteArray() 
            ?: message.toByteArray(Charsets.UTF_8).take(27).toByteArray()
            
        buffer.put(msgBytes)
        
        // Pad with zeros
        for (i in msgBytes.size until 27) {
            buffer.put(0)
        }
        
        val bytes = buffer.array()
        val crc = CRC16.calculate(bytes, 30)
        buffer.putShort(30, crc.toShort())
        
        return bytes
    }
}

/**
 * Represents an ACK received from a soldier node.
 * 8 bytes total.
 */
data class AckPacket(
    val magic1: Byte = 0x5A.toByte(),
    val magic2: Byte = 0xA6.toByte(),
    val senderNodeId: Int,
    val targetNodeId: Int,
    val commandId: Int, // Sequence or hash
    val crc16: Int
) {
    companion object {
        const val PACKET_SIZE = 8
        
        fun parse(bytes: ByteArray): AckPacket? {
            if (bytes.size < PACKET_SIZE) return null
            if (bytes[0] != 0x5A.toByte() || bytes[1] != 0xA6.toByte()) return null
            
            val calculatedCrc = CRC16.calculate(bytes, 6)
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val crc = buffer.getShort(6).toInt() and 0xFFFF
            
            if (calculatedCrc != crc) return null
            
            return AckPacket(
                magic1 = bytes[0],
                magic2 = bytes[1],
                senderNodeId = bytes[2].toInt() and 0xFF,
                targetNodeId = bytes[3].toInt() and 0xFF,
                commandId = buffer.getShort(4).toInt() and 0xFFFF,
                crc16 = crc
            )
        }
    }
}
