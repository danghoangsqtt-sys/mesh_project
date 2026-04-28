package com.meshcommand.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meshcommand.app.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY timestamp_ms DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 50): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE node_id = :nodeId ORDER BY timestamp_ms DESC LIMIT :limit")
    fun getEventsForNode(nodeId: Int, limit: Int = 20): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE event_type = 'ALERT' AND acknowledged = 0 ORDER BY timestamp_ms DESC")
    fun getUnacknowledgedAlerts(): Flow<List<EventEntity>>

    @Query("UPDATE events SET acknowledged = 1 WHERE id = :eventId")
    suspend fun acknowledgeEvent(eventId: Long)

    @Query("DELETE FROM events WHERE timestamp_ms < :thresholdMs")
    suspend fun deleteOlderThan(thresholdMs: Long)
}
