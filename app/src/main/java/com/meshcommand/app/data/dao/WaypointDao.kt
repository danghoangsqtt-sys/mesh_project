package com.meshcommand.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.meshcommand.app.data.entity.WaypointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Insert
    suspend fun insert(waypoint: WaypointEntity): Long

    @Update
    suspend fun update(waypoint: WaypointEntity)

    @Delete
    suspend fun delete(waypoint: WaypointEntity)

    @Query("SELECT * FROM waypoints ORDER BY created_at DESC")
    fun getAll(): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints WHERE assigned_node_id = :nodeId")
    fun getForNode(nodeId: Int): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints WHERE id = :waypointId")
    suspend fun getById(waypointId: Long): WaypointEntity?

    @Query("DELETE FROM waypoints WHERE id = :waypointId")
    suspend fun deleteById(waypointId: Long)
}
