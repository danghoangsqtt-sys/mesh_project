package com.meshcommand.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Geofence zone — can be "safe" (alert when soldier leaves)
 * or "restricted" (alert when soldier enters).
 */
@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "zone_type")
    val zoneType: String, // "safe" or "restricted"

    @ColumnInfo(name = "points_json")
    val pointsJson: String, // JSON array of [lat,lon] pairs

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_SAFE = "safe"
        const val TYPE_RESTRICTED = "restricted"
    }
}
