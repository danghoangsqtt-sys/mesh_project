package com.meshcommand.app.comm

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CommandPacketTest {

    @Test
    fun testCommandPacketSerialization() {
        val packet = CommandPacket(targetNodeId = 5, message = "HELLO")
        val bytes = packet.toByteArray()

        assertEquals(32, bytes.size)
        assertEquals(0x5A.toByte(), bytes[0])
        assertEquals(0xA5.toByte(), bytes[1])
        assertEquals(5.toByte(), bytes[2])
        
        // "HELLO" is 5 bytes
        assertEquals('H'.code.toByte(), bytes[3])
        assertEquals('E'.code.toByte(), bytes[4])
        assertEquals('L'.code.toByte(), bytes[5])
        assertEquals('L'.code.toByte(), bytes[6])
        assertEquals('O'.code.toByte(), bytes[7])
        
        // Next bytes should be zero padding
        assertEquals(0.toByte(), bytes[8])
        assertEquals(0.toByte(), bytes[29])
        
        // CRC test
        val expectedCrc = CRC16.calculate(bytes, 30)
        val actualCrc = ((bytes[31].toInt() and 0xFF) shl 8) or (bytes[30].toInt() and 0xFF)
        assertEquals(expectedCrc, actualCrc)
    }

    @Test
    fun testCommandPacketLongMessageTruncation() {
        val longMsg = "123456789012345678901234567890" // 30 chars
        val packet = CommandPacket(targetNodeId = 1, message = longMsg)
        val bytes = packet.toByteArray()
        
        assertEquals(32, bytes.size)
        // Ensure truncation at 27 bytes
        assertEquals('7'.code.toByte(), bytes[29]) // 27th byte (index 29) is '7'
        
        // Calculate CRC to ensure it covers exactly 30 bytes
        val expectedCrc = CRC16.calculate(bytes, 30)
        val actualCrc = ((bytes[31].toInt() and 0xFF) shl 8) or (bytes[30].toInt() and 0xFF)
        assertEquals(expectedCrc, actualCrc)
    }

    @Test
    fun testAckPacketParsing_Valid() {
        // Construct a valid ACK packet (8 bytes)
        val buffer = ByteArray(8)
        buffer[0] = 0x5A.toByte()
        buffer[1] = 0xA6.toByte()
        buffer[2] = 2 // sender
        buffer[3] = 0 // target
        buffer[4] = 0x12 // command ID LSB
        buffer[5] = 0x34 // command ID MSB
        
        val crc = CRC16.calculate(buffer, 6)
        buffer[6] = (crc and 0xFF).toByte()
        buffer[7] = ((crc shr 8) and 0xFF).toByte()
        
        val ack = AckPacket.parse(buffer)
        assertNotNull(ack)
        assertEquals(2, ack?.senderNodeId)
        assertEquals(0, ack?.targetNodeId)
        assertEquals(0x3412, ack?.commandId)
    }

    @Test
    fun testAckPacketParsing_InvalidCrc() {
        val buffer = ByteArray(8)
        buffer[0] = 0x5A.toByte()
        buffer[1] = 0xA6.toByte()
        buffer[2] = 2 // sender
        buffer[3] = 0 // target
        buffer[4] = 0x12 // command ID LSB
        buffer[5] = 0x34 // command ID MSB
        buffer[6] = 0x00 // Invalid CRC
        buffer[7] = 0x00 // Invalid CRC
        
        val ack = AckPacket.parse(buffer)
        assertNull(ack)
    }
}
