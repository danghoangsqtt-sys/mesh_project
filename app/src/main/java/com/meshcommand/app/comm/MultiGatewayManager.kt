package com.meshcommand.app.comm

import android.util.Log
import com.meshcommand.app.data.SoldierRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-Gateway Manager — handles simultaneous connections to multiple gateways
 * (USB + WiFi). Merges packet streams and de-duplicates by (nodeId + timestamp).
 *
 * Usage: call feedPacket() from each gateway's collection loop.
 * The manager de-duplicates and forwards to the repository.
 */
@Singleton
class MultiGatewayManager @Inject constructor(
    private val soldierRepository: SoldierRepository
) {
    companion object {
        private const val TAG = "MultiGatewayManager"
        private const val DEDUP_WINDOW_MS = 2000L
    }

    data class GatewayInfo(
        val id: String,
        val type: String, // "USB" or "WiFi"
        val state: ConnectionState,
        val packetsReceived: Long = 0
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // De-duplication cache: key = "nodeId:timestamp" -> insertion time
    private val deduplicationCache = ConcurrentHashMap<String, Long>()

    // Merged packet output
    private val _mergedPacketFlow = MutableSharedFlow<SoldierPacket>(extraBufferCapacity = 64)
    val mergedPacketFlow: SharedFlow<SoldierPacket> = _mergedPacketFlow.asSharedFlow()

    private val _gateways = MutableStateFlow<List<GatewayInfo>>(emptyList())
    val gateways: StateFlow<List<GatewayInfo>> = _gateways.asStateFlow()

    private val packetCounts = ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong>()

    init {
        // Start periodic cleanup
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(10_000)
                cleanupDedupCache()
            }
        }
    }

    /**
     * Feed a packet from any gateway source. De-duplicates and forwards to repository.
     * @param gatewayId unique gateway identifier (e.g. "usb-primary", "wifi-192.168.4.1")
     * @param gatewayType "USB" or "WiFi"
     */
    suspend fun feedPacket(packet: SoldierPacket, gatewayId: String, gatewayType: String) {
        if (isDuplicate(packet)) return

        packetCounts.getOrPut(gatewayId) { java.util.concurrent.atomic.AtomicLong(0) }
            .incrementAndGet()

        soldierRepository.processPacket(packet)
        _mergedPacketFlow.tryEmit(packet)
    }

    fun updateGatewayState(gatewayId: String, gatewayType: String, state: ConnectionState) {
        val current = _gateways.value.toMutableList()
        val existing = current.indexOfFirst { it.id == gatewayId }
        val info = GatewayInfo(
            id = gatewayId,
            type = gatewayType,
            state = state,
            packetsReceived = packetCounts[gatewayId]?.get() ?: 0
        )

        if (existing >= 0) {
            current[existing] = info
        } else {
            current.add(info)
        }
        _gateways.value = current
    }

    private fun isDuplicate(packet: SoldierPacket): Boolean {
        val key = "${packet.nodeId}:${packet.timestamp}"
        val now = System.currentTimeMillis()
        val existing = deduplicationCache.putIfAbsent(key, now)
        if (existing != null) {
            Log.d(TAG, "Duplicate packet dropped: node=${packet.nodeId}, ts=${packet.timestamp}")
            return true
        }
        return false
    }

    private fun cleanupDedupCache() {
        val threshold = System.currentTimeMillis() - DEDUP_WINDOW_MS
        deduplicationCache.entries.removeIf { it.value < threshold }
    }
}
