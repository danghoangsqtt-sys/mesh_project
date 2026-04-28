package com.meshcommand.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.meshcommand.app.data.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Insert
    suspend fun insert(team: TeamEntity): Long

    @Insert
    suspend fun insertAll(teams: List<TeamEntity>)

    @Update
    suspend fun update(team: TeamEntity)

    @Delete
    suspend fun delete(team: TeamEntity)

    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAll(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getById(teamId: Long): TeamEntity?

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun getCount(): Int
}
