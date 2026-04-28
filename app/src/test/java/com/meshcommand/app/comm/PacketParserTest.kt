package com.meshcommand.app.comm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PacketParserTest {
    @Test
    fun testBuildAndParse() {
        val original = SoldierPacket(
            nodeId = 1234,
            timestamp = 1610000000L,
            latitude = 21.0285f,
            longitude = 105.8542f,
            heading = 90.5f,
            heartRate = 75,
            spo2 = 98,
            temperature = 36.5f,
            humidity = 60.0f,
            pressure = 1013.25f,
            batteryVoltage = 4.1f,
            statusFlags = SoldierPacket.GPS_FIX or SoldierPacket.HR_VALID
        )

        val bytes = PacketParser.build(original)
        assertEquals(SoldierPacket.PACKET_SIZE, bytes.size)

        val parsed = PacketParser.parse(bytes)
        assertNotNull(parsed)
        
        assertEquals(original.nodeId, parsed!!.nodeId)
        assertEquals(original.timestamp, parsed.timestamp)
        assertEquals(original.latitude, parsed.latitude, 0.0001f)
        assertEquals(original.longitude, parsed.longitude, 0.0001f)
        assertEquals(original.heartRate, parsed.heartRate)
        assertEquals(original.spo2, parsed.spo2)
        assertEquals(original.statusFlags, parsed.statusFlags)
        
        // Test CRC corruption
        bytes[0] = (bytes[0].toInt() xor 0xFF).toByte()
        val corruptParsed = PacketParser.parse(bytes)
        assertNull(corruptParsed)
    }
    
    @Test
    fun testFrameExtractor() {
        val packet = SoldierPacket(
            nodeId = 1,
            timestamp = 1000L,
            latitude = 0f,
            longitude = 0f,
            heading = 0f,
            heartRate = 60,
            spo2 = 95,
            temperature = 36f,
            humidity = 50f,
            pressure = 1000f,
            batteryVoltage = 4.0f,
            statusFlags = 0
        )
        val packetBytes = PacketParser.build(packet)
        
        var receivedPacket: SoldierPacket? = null
        var receivedGwGps: Pair<Double, Double>? = null
        
        val extractor = FrameExtractor(
            onPacketReceived = { receivedPacket = it },
            onGatewayGpsReceived = { lat, lon -> receivedGwGps = lat to lon }
        )
        
        // Test sideband GPS
        extractor.append("GW_GPS:21.0,105.0\n".toByteArray(Charsets.US_ASCII))
        assertEquals(21.0, receivedGwGps!!.first, 0.001)
        assertEquals(105.0, receivedGwGps!!.second, 0.001)
        
        // Test packet with framing and some garbage before it
        extractor.append(byteArrayOf(0x00, 0xFF.toByte()))
        extractor.append(byteArrayOf(FrameExtractor.HEADER_BYTE))
        extractor.append(packetBytes)
        extractor.append(byteArrayOf(FrameExtractor.FOOTER_BYTE, 0x00))
        
        assertNotNull(receivedPacket)
        assertEquals(1, receivedPacket!!.nodeId)
    }
}
