package com.meshcommand.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meshcommand.app.data.dao.EventDao
import com.meshcommand.app.data.dao.GeofenceDao
import com.meshcommand.app.data.dao.PositionHistoryDao
import com.meshcommand.app.data.dao.SoldierDao
import com.meshcommand.app.data.dao.TeamDao
import com.meshcommand.app.data.dao.WaypointDao
import com.meshcommand.app.data.entity.EventEntity
import com.meshcommand.app.data.entity.GeofenceEntity
import com.meshcommand.app.data.entity.PositionHistoryEntity
import com.meshcommand.app.data.entity.SoldierEntity
import com.meshcommand.app.data.entity.TeamEntity
import com.meshcommand.app.data.entity.WaypointEntity

@Database(
    entities = [
        SoldierEntity::class,
        EventEntity::class,
        PositionHistoryEntity::class,
        TeamEntity::class,
        GeofenceEntity::class,
        WaypointEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun soldierDao(): SoldierDao
    abstract fun eventDao(): EventDao
    abstract fun positionHistoryDao(): PositionHistoryDao
    abstract fun teamDao(): TeamDao
    abstract fun geofenceDao(): GeofenceDao
    abstract fun waypointDao(): WaypointDao
}
