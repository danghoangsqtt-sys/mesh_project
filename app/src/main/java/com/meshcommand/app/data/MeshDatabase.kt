package com.meshcommand.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meshcommand.app.data.dao.EventDao
import com.meshcommand.app.data.dao.SoldierDao
import com.meshcommand.app.data.entity.EventEntity
import com.meshcommand.app.data.entity.SoldierEntity

@Database(
    entities = [SoldierEntity::class, EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun soldierDao(): SoldierDao
    abstract fun eventDao(): EventDao
}
