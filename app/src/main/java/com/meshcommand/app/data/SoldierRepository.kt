package com.meshcommand.app.data

import android.util.Log
import com.meshcommand.app.comm.SoldierPacket
import com.meshcommand.app.data.dao.EventDao
import com.meshcommand.app.data.dao.GeofenceDao
import com.meshcommand.app.data.dao.PositionHistoryDao
import com.meshcommand.app.data.dao.SoldierDao
import com.meshcommand.app.data.entity.EventEntity
import com.meshcommand.app.data.entity.PositionHistoryEntity
import com.meshcommand.app.data.entity.SoldierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoldierRepository @Inject constructor(
    private val soldierDao: SoldierDao,
    private val eventDao: EventDao,
    private val positionHistoryDao: PositionHistoryDao,
    private val geofenceDao: GeofenceDao
) {
    companion object {
        private const val TAG = "SoldierRepository"
    }

    // ─────────────────────────────────────────────
    // Reactive Queries (exposed to ViewModels)
    // ─────────────────────────────────────────────

    val allSoldiers: Flow<List<SoldierEntity>> = soldierDao.getAll()

    val onlineSoldiers: Flow<List<SoldierEntity>> = soldierDao.getOnlineSoldiers()

    val onlineCount: Flow<Int> = soldierDao.getOnlineCount()

    val criticalSoldiers: Flow<List<SoldierEntity>> = soldierDao.getCriticalSoldiers()

    val recentEvents: Flow<List<EventEntity>> = eventDao.getRecentEvents(50)

    val unacknowledgedAlerts: Flow<List<EventEntity>> = eventDao.getUnacknowledgedAlerts()

    fun getSoldierById(nodeId: Int): Flow<SoldierEntity?> = soldierDao.getByNodeId(nodeId)

    fun getEventsForNode(nodeId: Int): Flow<List<EventEntity>> = eventDao.getEventsForNode(nodeId)

    fun getTrailForNode(nodeId: Int, sinceMs: Long): Flow<List<PositionHistoryEntity>> =
        positionHistoryDao.getTrailForNode(nodeId, sinceMs)

    fun getAllTrails(sinceMs: Long): Flow<List<PositionHistoryEntity>> {
        return positionHistoryDao.getAllTrails(sinceMs)
    }

    // Command Queue for Gateway Sync
    val outgoingCommands = MutableSharedFlow<String>(extraBufferCapacity = 10)

    // Geofences
    val activeGeofences: Flow<List<com.meshcommand.app.data.entity.GeofenceEntity>> = geofenceDao.getActiveGeofences()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val activeGeofencesCache = mutableListOf<com.meshcommand.app.data.entity.GeofenceEntity>()

    init {
        repositoryScope.launch {
            activeGeofences.collect { list ->
                activeGeofencesCache.clear()
                activeGeofencesCache.addAll(list)
            }
        }
    }

    suspend fun saveGeofence(geofence: com.meshcommand.app.data.entity.GeofenceEntity) = withContext(Dispatchers.IO) {
        geofenceDao.insert(geofence)
    }

    suspend fun deleteGeofence(geofence: com.meshcommand.app.data.entity.GeofenceEntity) = withContext(Dispatchers.IO) {
        geofenceDao.delete(geofence)
    }

    // ─────────────────────────────────────────────
    // Packet Processing
    // ─────────────────────────────────────────────

    suspend fun processPacket(packet: SoldierPacket) = withContext(Dispatchers.IO) {
        var entity = SoldierEntity.fromPacket(packet)

        // ─── Phase 8: Real-time Geofence Checking ───
        if (entity.latitude != 0.0 && entity.longitude != 0.0) {
            val violations = com.meshcommand.app.tactical.GeofenceChecker.checkViolations(
                entity.latitude,
                entity.longitude,
                activeGeofencesCache
            )
            
            if (violations.isNotEmpty()) {
                val firstViolated = violations.first()
                val reason = if (firstViolated.zoneType == com.meshcommand.app.data.entity.GeofenceEntity.TYPE_RESTRICTED) {
                    "Entered DANGER zone: ${firstViolated.name}"
                } else {
                    "Left SAFE zone: ${firstViolated.name}"
                }
                
                // Elevate alert level
                entity = entity.copy(alertLevel = 2)
                
                // Generate Geofence Alert Event
                eventDao.insert(
                    EventEntity(
                        eventType = "GEOFENCE_ALERT",
                        severity = 2,
                        nodeId = packet.nodeId,
                        message = "Node ${packet.nodeId}: $reason"
                    )
                )

                // Trigger SOS Broadcast to all nodes
                repositoryScope.launch {
                    outgoingCommands.emit("[BROADCAST] CMD:SOS_TRIGGER")
                }
            }
        }

        soldierDao.upsert(entity)

        Log.d(TAG, "Processed packet: node=${packet.nodeId}, alert=${entity.alertLevel}")

        // Save position history for trail
        if (packet.latitude != 0f && packet.longitude != 0f) {
            positionHistoryDao.insert(
                PositionHistoryEntity(
                    nodeId = packet.nodeId,
                    latitude = packet.latitude.toDouble(),
                    longitude = packet.longitude.toDouble(),
                    heading = packet.heading.toDouble()
                )
            )
        }

        // Generate alert events for critical states
        if (entity.alertLevel >= 2) {
            val alertMessages = mutableListOf<String>()
            if (packet.statusFlags and SoldierPacket.MAN_DOWN != 0) {
                alertMessages.add("MAN DOWN detected")
            }
            if (packet.statusFlags and SoldierPacket.ALERT != 0) {
                alertMessages.add("SOS ALERT triggered")
            }
            if (packet.statusFlags and SoldierPacket.CRITICAL_BATTERY != 0) {
                alertMessages.add("CRITICAL battery level")
            }

            for (msg in alertMessages) {
                eventDao.insert(
                    EventEntity(
                        eventType = "ALERT",
                        severity = 2,
                        nodeId = packet.nodeId,
                        message = "Node ${packet.nodeId}: $msg"
                    )
                )
            }
        }

        // Warning events
        if (entity.alertLevel == 1) {
            if (packet.statusFlags and SoldierPacket.LOW_BATTERY != 0) {
                eventDao.insert(
                    EventEntity(
                        eventType = "ALERT",
                        severity = 1,
                        nodeId = packet.nodeId,
                        message = "Node ${packet.nodeId}: Low battery (${packet.batteryVoltage}V)"
                    )
                )
            }
            if (packet.statusFlags and SoldierPacket.HEAT_STRESS != 0) {
                eventDao.insert(
                    EventEntity(
                        eventType = "ALERT",
                        severity = 1,
                        nodeId = packet.nodeId,
                        message = "Node ${packet.nodeId}: Heat stress detected"
                    )
                )
            }
        }
    }

    // ─────────────────────────────────────────────
    // Node Timeout Check
    // ─────────────────────────────────────────────

    suspend fun checkTimeouts() = withContext(Dispatchers.IO) {
        val thresholdMs = System.currentTimeMillis() - SoldierEntity.TIMEOUT_MS
        soldierDao.markOffline(thresholdMs)
    }

    // ─────────────────────────────────────────────
    // Event Management
    // ─────────────────────────────────────────────

    suspend fun logEvent(
        eventType: String,
        message: String,
        severity: Int = 0,
        nodeId: Int? = null
    ) = withContext(Dispatchers.IO) {
        eventDao.insert(
            EventEntity(
                eventType = eventType,
                severity = severity,
                nodeId = nodeId,
                message = message
            )
        )
    }

    suspend fun acknowledgeAlert(eventId: Long) = withContext(Dispatchers.IO) {
        eventDao.acknowledgeEvent(eventId)
    }

    suspend fun cleanupOldEvents(maxAgeMs: Long = 24 * 60 * 60 * 1000L) = withContext(Dispatchers.IO) {
        val thresholdMs = System.currentTimeMillis() - maxAgeMs
        eventDao.deleteOlderThan(thresholdMs)
    }
}
