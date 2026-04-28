package com.meshcommand.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: String, // hex color e.g. "#FF0000"

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT_TEAMS = listOf(
            TeamEntity(name = "Alpha", color = "#4CAF50"),
            TeamEntity(name = "Bravo", color = "#2196F3"),
            TeamEntity(name = "Charlie", color = "#FF9800"),
            TeamEntity(name = "Delta", color = "#9C27B0"),
            TeamEntity(name = "Echo", color = "#00BCD4"),
            TeamEntity(name = "Foxtrot", color = "#E91E63")
        )
    }
}
