package com.meshcommand.app.comm

object CRC16 {
    fun calculate(data: ByteArray, length: Int): Int {
        var crc = 0xFFFF
        for (i in 0 until length) {
            crc = crc xor ((data[i].toInt() and 0xFF) shl 8)
            for (j in 0 until 8) {
                if ((crc and 0x8000) != 0) {
                    crc = ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    crc = (crc shl 1) and 0xFFFF
                }
            }
        }
        return crc and 0xFFFF
    }
}
