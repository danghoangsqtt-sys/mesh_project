package com.meshcommand.app.comm

import java.nio.charset.StandardCharsets

class FrameExtractor(
    private val onPacketReceived: (SoldierPacket) -> Unit,
    private val onGatewayGpsReceived: (lat: Double, lon: Double) -> Unit,
    private val onAckReceived: ((AckPacket) -> Unit)? = null
) {
    private val buffer = mutableListOf<Byte>()
    
    companion object {
        const val HEADER_BYTE = 0xAA.toByte()
        const val FOOTER_BYTE = 0x55.toByte()
        const val PACKET_SIZE_WITH_FRAMING = SoldierPacket.PACKET_SIZE + 2 // 42
        
        const val ACK_MAGIC1 = 0x5A.toByte()
        const val ACK_MAGIC2 = 0xA6.toByte()
        const val ACK_SIZE = 8
    }

    fun append(bytes: ByteArray) {
        for (b in bytes) {
            buffer.add(b)
        }
        processBuffer()
    }

    private fun processBuffer() {
        while (buffer.isNotEmpty()) {
            if (processGatewayGps()) continue

            // Find first occurrence of either telemetry header or ACK header
            val headerIndex = buffer.indexOf(HEADER_BYTE)
            var ackIndex = -1
            
            for (i in 0 until buffer.size - 1) {
                if (buffer[i] == ACK_MAGIC1 && buffer[i+1] == ACK_MAGIC2) {
                    ackIndex = i
                    break
                }
            }

            // If neither found, clear buffer (except partial GW_GPS or ACK_MAGIC1 at the end)
            if (headerIndex == -1 && ackIndex == -1) {
                val gwIndex = indexOfSubList(buffer, "GW_".toByteArray(StandardCharsets.US_ASCII))
                if (gwIndex != -1) {
                    if (gwIndex > 0) buffer.subList(0, gwIndex).clear()
                } else if (buffer.last() == ACK_MAGIC1) {
                    buffer.subList(0, buffer.size - 1).clear()
                } else {
                    buffer.clear()
                }
                return
            }

            // Determine which packet starts first
            val processAckFirst = ackIndex != -1 && (headerIndex == -1 || ackIndex < headerIndex)

            if (processAckFirst) {
                if (ackIndex > 0) buffer.subList(0, ackIndex).clear()
                
                if (buffer.size >= ACK_SIZE) {
                    val ackData = buffer.subList(0, ACK_SIZE).toByteArray()
                    val ack = AckPacket.parse(ackData)
                    if (ack != null) {
                        onAckReceived?.invoke(ack)
                        buffer.subList(0, ACK_SIZE).clear()
                    } else {
                        buffer.removeAt(0) // Invalid ACK, drop magic1
                    }
                } else {
                    return // Wait for more bytes
                }
            } else {
                if (headerIndex > 0) buffer.subList(0, headerIndex).clear()

                if (buffer.size >= PACKET_SIZE_WITH_FRAMING) {
                    val footerIndex = PACKET_SIZE_WITH_FRAMING - 1
                    if (buffer[footerIndex] == FOOTER_BYTE) {
                        val packetData = buffer.subList(1, footerIndex).toByteArray()
                        val packet = PacketParser.parse(packetData)
                        if (packet != null) {
                            onPacketReceived(packet)
                        }
                        buffer.subList(0, PACKET_SIZE_WITH_FRAMING).clear()
                    } else {
                        buffer.removeAt(0) // Invalid footer, drop header
                    }
                } else {
                    return // Wait for more bytes
                }
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
