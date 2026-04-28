package com.meshcommand.app.comm

import java.nio.charset.StandardCharsets

class FrameExtractor(
    private val onPacketReceived: (SoldierPacket) -> Unit,
    private val onGatewayGpsReceived: (lat: Double, lon: Double) -> Unit
) {
    private val buffer = mutableListOf<Byte>()
    
    companion object {
        const val HEADER_BYTE = 0xAA.toByte()
        const val FOOTER_BYTE = 0x55.toByte()
        const val PACKET_SIZE_WITH_FRAMING = SoldierPacket.PACKET_SIZE + 2 // header + data + footer = 1 + 40 + 1 = 42
    }

    fun append(bytes: ByteArray) {
        for (b in bytes) {
            buffer.add(b)
        }
        processBuffer()
    }

    private fun processBuffer() {
        while (buffer.isNotEmpty()) {
            // Check for Gateway GPS string "GW_GPS:lat,lon\n"
            if (processGatewayGps()) {
                continue
            }

            // Find header byte
            val headerIndex = buffer.indexOf(HEADER_BYTE)
            if (headerIndex == -1) {
                // No header found, clear buffer except possible partial "GW_GPS"
                val gwIndex = indexOfSubList(buffer, "GW_".toByteArray(StandardCharsets.US_ASCII))
                if (gwIndex != -1) {
                    if (gwIndex > 0) buffer.subList(0, gwIndex).clear()
                } else {
                    buffer.clear()
                }
                return
            }

            // Remove garbage before header
            if (headerIndex > 0) {
                buffer.subList(0, headerIndex).clear()
            }

            // Check if we have enough bytes for a full framed packet
            if (buffer.size >= PACKET_SIZE_WITH_FRAMING) {
                val footerIndex = PACKET_SIZE_WITH_FRAMING - 1
                if (buffer[footerIndex] == FOOTER_BYTE) {
                    val packetData = buffer.subList(1, footerIndex).toByteArray()
                    val packet = PacketParser.parse(packetData)
                    if (packet != null) {
                        onPacketReceived(packet)
                    }
                    // Remove processed frame
                    buffer.subList(0, PACKET_SIZE_WITH_FRAMING).clear()
                } else {
                    // Invalid footer, drop header byte and continue searching
                    buffer.removeAt(0)
                }
            } else {
                // Not enough bytes yet
                return
            }
        }
    }

    private fun processGatewayGps(): Boolean {
        val newlineIndex = buffer.indexOf('\n'.code.toByte())
        if (newlineIndex != -1) {
            val lineBytes = buffer.subList(0, newlineIndex).toByteArray()
            val line = String(lineBytes, StandardCharsets.US_ASCII)
            if (line.startsWith("GW_GPS:")) {
                try {
                    val parts = line.substring(7).split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].toDouble()
                        val lon = parts[1].toDouble()
                        onGatewayGpsReceived(lat, lon)
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
                buffer.subList(0, newlineIndex + 1).clear()
                return true
            }
        }
        return false
    }

    private fun indexOfSubList(list: List<Byte>, subList: ByteArray): Int {
        if (subList.isEmpty() || list.size < subList.size) return -1
        for (i in 0..list.size - subList.size) {
            var match = true
            for (j in subList.indices) {
                if (list[i + j] != subList[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }
}
