package com.meshcommand.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meshcommand.app.data.dao.EventDao
import com.meshcommand.app.data.dao.PositionHistoryDao
import com.meshcommand.app.data.dao.SoldierDao
import com.meshcommand.app.data.entity.EventEntity
import com.meshcommand.app.data.entity.PositionHistoryEntity
import com.meshcommand.app.data.entity.SoldierEntity

@Database(
    entities = [SoldierEntity::class, EventEntity::class, PositionHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun soldierDao(): SoldierDao
    abstract fun eventDao(): EventDao
    abstract fun positionHistoryDao(): PositionHistoryDao
}
