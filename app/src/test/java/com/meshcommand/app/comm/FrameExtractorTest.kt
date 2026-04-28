package com.meshcommand.app.comm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FrameExtractorTest {

    @Test
    fun testSoldierTelemetryPacketExtraction() {
        var receivedPacket: SoldierPacket? = null
        val extractor = FrameExtractor(
            onPacketReceived = { receivedPacket = it },
            onGatewayGpsReceived = { _, _ -> },
            onAckReceived = { }
        )

        // Construct a valid 40-byte telemetry packet
        val buffer = ByteArray(40)
        buffer[0] = 0xAA.toByte()
        buffer[1] = 0x55.toByte()
        buffer[2] = 1 // nodeId
        buffer[38] = 0 // CRC placeholder
        buffer[39] = 0 // CRC placeholder
        
        val crc = CRC16.calculate(buffer, 38)
        buffer[38] = (crc and 0xFF).toByte()
        buffer[39] = ((crc shr 8) and 0xFF).toByte()

        // Feed whole buffer
        extractor.append(buffer)

        assertNotNull("Packet should be extracted", receivedPacket)
        assertEquals(1, receivedPacket?.nodeId)
    }

    @Test
    fun testAckPacketExtraction() {
        var receivedAck: AckPacket? = null
        val extractor = FrameExtractor(
            onPacketReceived = { },
            onGatewayGpsReceived = { _, _ -> },
            onAckReceived = { receivedAck = it }
        )

        val buffer = ByteArray(8)
        buffer[0] = 0x5A.toByte()
        buffer[1] = 0xA6.toByte()
        buffer[2] = 2 // sender
        buffer[3] = 0 // target
        buffer[4] = 0x12 // LSB
        buffer[5] = 0x34 // MSB
        
        val crc = CRC16.calculate(buffer, 6)
        buffer[6] = (crc and 0xFF).toByte()
        buffer[7] = ((crc shr 8) and 0xFF).toByte()

        // Feed whole buffer
        extractor.append(buffer)

        assertNotNull("ACK should be extracted", receivedAck)
        assertEquals(2, receivedAck?.senderNodeId)
        assertEquals(0x3412, receivedAck?.commandId)
    }

    @Test
    fun testGarbageBeforePacket() {
        var receivedAck: AckPacket? = null
        val extractor = FrameExtractor(
            onPacketReceived = { },
            onGatewayGpsReceived = { _, _ -> },
            onAckReceived = { receivedAck = it }
        )

        val buffer = ByteArray(8)
        buffer[0] = 0x5A.toByte()
        buffer[1] = 0xA6.toByte()
        buffer[2] = 5 // sender
        buffer[3] = 0 // target
        buffer[4] = 0x00
        buffer[5] = 0x00
        val crc = CRC16.calculate(buffer, 6)
        buffer[6] = (crc and 0xFF).toByte()
        buffer[7] = ((crc shr 8) and 0xFF).toByte()

        // Feed garbage
        extractor.append(byteArrayOf(0x11.toByte(), 0x22.toByte(), 0x5A.toByte(), 0x11.toByte()))

        // Feed valid packet
        extractor.append(buffer)

        assertNotNull("ACK should be extracted despite earlier garbage", receivedAck)
        assertEquals(5, receivedAck?.senderNodeId)
    }
}
