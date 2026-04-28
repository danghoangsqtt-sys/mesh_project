package com.meshcommand.app.comm

import org.junit.Assert.assertEquals
import org.junit.Test

class CRC16Test {
    @Test
    fun testCRC16CCITT() {
        // Test vector 1: "123456789" -> 0x29B1
        val input = "123456789".toByteArray(Charsets.US_ASCII)
        val crc = CRC16.calculate(input, input.size)
        assertEquals(0x29B1, crc)
        
        // Test vector 2: empty array -> 0xFFFF
        val emptyInput = ByteArray(0)
        val emptyCrc = CRC16.calculate(emptyInput, 0)
        assertEquals(0xFFFF, emptyCrc)
        
        // Test vector 3: single byte 'A' -> 0xB915
        val singleByte = byteArrayOf('A'.code.toByte())
        val singleCrc = CRC16.calculate(singleByte, 1)
        assertEquals(0xB915, singleCrc)
    }
}
