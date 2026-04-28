package com.meshcommand.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meshcommand.app.data.entity.SoldierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoldierDao {
    @Upsert
    suspend fun upsert(soldier: SoldierEntity)

    @Query("SELECT * FROM soldiers ORDER BY alert_level DESC, node_id ASC")
    fun getAll(): Flow<List<SoldierEntity>>

    @Query("SELECT * FROM soldiers WHERE node_id = :nodeId")
    fun getByNodeId(nodeId: Int): Flow<SoldierEntity?>

    @Query("SELECT * FROM soldiers WHERE is_online = 1 ORDER BY node_id ASC")
    fun getOnlineSoldiers(): Flow<List<SoldierEntity>>

    @Query("SELECT COUNT(*) FROM soldiers WHERE is_online = 1")
    fun getOnlineCount(): Flow<Int>

    @Query("UPDATE soldiers SET is_online = 0, alert_level = 0 WHERE last_seen_ms < :thresholdMs AND is_online = 1")
    suspend fun markOffline(thresholdMs: Long)

    @Query("SELECT * FROM soldiers WHERE alert_level >= 2 AND is_online = 1")
    fun getCriticalSoldiers(): Flow<List<SoldierEntity>>
}
