package com.meshcommand.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.meshcommand.app.data.entity.GeofenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {
    @Insert
    suspend fun insert(geofence: GeofenceEntity): Long

    @Update
    suspend fun update(geofence: GeofenceEntity)

    @Delete
    suspend fun delete(geofence: GeofenceEntity)

    @Query("SELECT * FROM geofences ORDER BY created_at DESC")
    fun getAll(): Flow<List<GeofenceEntity>>

    @Query("SELECT * FROM geofences WHERE is_active = 1")
    fun getActiveGeofences(): Flow<List<GeofenceEntity>>

    @Query("SELECT * FROM geofences WHERE id = :geofenceId")
    suspend fun getById(geofenceId: Long): GeofenceEntity?
}
