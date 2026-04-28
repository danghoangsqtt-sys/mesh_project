package com.meshcommand.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meshcommand.app.comm.SoldierPacket

@Entity(tableName = "soldiers")
data class SoldierEntity(
    @PrimaryKey
    @ColumnInfo(name = "node_id")
    val nodeId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String = "unassigned",

    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "heading")
    val heading: Double = 0.0,

    @ColumnInfo(name = "heart_rate")
    val heartRate: Int = 0,

    @ColumnInfo(name = "spo2")
    val spo2: Int = 0,

    @ColumnInfo(name = "temperature")
    val temperature: Double = 0.0,

    @ColumnInfo(name = "humidity")
    val humidity: Double = 0.0,

    @ColumnInfo(name = "pressure")
    val pressure: Double = 0.0,

    @ColumnInfo(name = "battery_volts")
    val batteryVolts: Double = 0.0,

    @ColumnInfo(name = "status_flags")
    val statusFlags: Int = 0,

    @ColumnInfo(name = "alert_level")
    val alertLevel: Int = 0, // 0=OK, 1=WARN, 2=CRIT

    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,

    @ColumnInfo(name = "last_seen_ms")
    val lastSeenMs: Long = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TIMEOUT_MS = 30_000L // 30 seconds → offline

        fun fromPacket(packet: SoldierPacket): SoldierEntity {
            val alertLevel = calculateAlertLevel(packet.statusFlags, packet.batteryVoltage)
            return SoldierEntity(
                nodeId = packet.nodeId,
                latitude = packet.latitude.toDouble(),
                longitude = packet.longitude.toDouble(),
                heading = packet.heading.toDouble(),
                heartRate = packet.heartRate,
                spo2 = packet.spo2,
                temperature = packet.temperature.toDouble(),
                humidity = packet.humidity.toDouble(),
                pressure = packet.pressure.toDouble(),
                batteryVolts = packet.batteryVoltage.toDouble(),
                statusFlags = packet.statusFlags,
                alertLevel = alertLevel,
                isOnline = true,
                lastSeenMs = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        fun calculateAlertLevel(statusFlags: Int, batteryVoltage: Float): Int {
            // Level 2 — CRITICAL
            if (statusFlags and SoldierPacket.MAN_DOWN != 0) return 2
            if (statusFlags and SoldierPacket.ALERT != 0) return 2
            if (statusFlags and SoldierPacket.CRITICAL_BATTERY != 0) return 2

            // Level 1 — WARNING
            if (statusFlags and SoldierPacket.LOW_BATTERY != 0) return 1
            if (statusFlags and SoldierPacket.SENSOR_ERROR != 0) return 1
            if (statusFlags and SoldierPacket.HEAT_STRESS != 0) return 1
            if (batteryVoltage in 0.01f..3.5f) return 1

            // Level 0 — OK
            return 0
        }
    }
}
