package com.meshcommand.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "event_type")
    val eventType: String, // "ALERT", "COMMAND", "CONNECTION", "SYSTEM"

    @ColumnInfo(name = "severity")
    val severity: Int = 0, // 0=INFO, 1=WARN, 2=ERROR

    @ColumnInfo(name = "node_id")
    val nodeId: Int? = null,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "acknowledged")
    val acknowledged: Boolean = false
)
