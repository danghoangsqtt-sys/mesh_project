package com.meshcommand.app.comm

import java.nio.ByteBuffer
import java.nio.ByteOrder

object PacketParser {
    
    fun parse(bytes: ByteArray): SoldierPacket? {
        if (bytes.size != SoldierPacket.PACKET_SIZE) return null
        
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        
        val packet = SoldierPacket(
            nodeId = buffer.short.toInt() and 0xFFFF,
            timestamp = buffer.int.toLong() and 0xFFFFFFFFL,
            latitude = buffer.float,
            longitude = buffer.float,
            heading = buffer.float,
            heartRate = buffer.get().toInt() and 0xFF,
            spo2 = buffer.get().toInt() and 0xFF,
            temperature = buffer.float,
            humidity = buffer.float,
            pressure = buffer.float,
            batteryVoltage = buffer.float,
            statusFlags = buffer.short.toInt() and 0xFFFF,
            crc16 = buffer.short.toInt() and 0xFFFF
        )
        
        val calculatedCrc = CRC16.calculate(bytes, SoldierPacket.PACKET_SIZE - 2)
        if (calculatedCrc != packet.crc16) {
            return null
        }
        
        return packet
    }

    fun build(packet: SoldierPacket): ByteArray {
        val buffer = ByteBuffer.allocate(SoldierPacket.PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        
        buffer.putShort(packet.nodeId.toShort())
        buffer.putInt(packet.timestamp.toInt())
        buffer.putFloat(packet.latitude)
        buffer.putFloat(packet.longitude)
        buffer.putFloat(packet.heading)
        buffer.put(packet.heartRate.toByte())
        buffer.put(packet.spo2.toByte())
        buffer.putFloat(packet.temperature)
        buffer.putFloat(packet.humidity)
        buffer.putFloat(packet.pressure)
        buffer.putFloat(packet.batteryVoltage)
        buffer.putShort(packet.statusFlags.toShort())
        
        val bytes = buffer.array()
        val crc = CRC16.calculate(bytes, SoldierPacket.PACKET_SIZE - 2)
        buffer.putShort(crc.toShort())
        
        return buffer.array()
    }
}
