package com.meshcommand.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meshcommand.app.data.entity.PositionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionHistoryDao {
    @Insert
    suspend fun insert(position: PositionHistoryEntity)

    @Query("SELECT * FROM position_history WHERE node_id = :nodeId AND timestamp_ms >= :sinceMs ORDER BY timestamp_ms ASC")
    fun getTrailForNode(nodeId: Int, sinceMs: Long): Flow<List<PositionHistoryEntity>>

    @Query("SELECT * FROM position_history WHERE timestamp_ms >= :sinceMs ORDER BY node_id, timestamp_ms ASC")
    fun getAllTrails(sinceMs: Long): Flow<List<PositionHistoryEntity>>

    @Query("DELETE FROM position_history WHERE timestamp_ms < :thresholdMs")
    suspend fun deleteOlderThan(thresholdMs: Long)

    @Query("SELECT COUNT(*) FROM position_history")
    suspend fun getCount(): Int
}
