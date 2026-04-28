package com.meshcommand.app.tactical

import com.meshcommand.app.data.dao.PositionHistoryDao
import com.meshcommand.app.data.entity.PositionHistoryEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mission Replay engine — plays back recorded position history
 * with configurable speed control and timeline scrubbing.
 */
@Singleton
class MissionReplayEngine @Inject constructor(
    private val positionHistoryDao: PositionHistoryDao
) {
    data class ReplayState(
        val isPlaying: Boolean = false,
        val currentTimeMs: Long = 0,
        val startTimeMs: Long = 0,
        val endTimeMs: Long = 0,
        val speed: Float = 1.0f,
        val currentPositions: Map<Int, PositionHistoryEntity> = emptyMap()
    )

    private val _state = MutableStateFlow(ReplayState())
    val state: StateFlow<ReplayState> = _state.asStateFlow()

    private var allPositions: List<PositionHistoryEntity> = emptyList()
    @Volatile
    private var isRunning = false

    /**
     * Load positions for a given time range.
     */
    suspend fun loadMission(startMs: Long, endMs: Long) {
        allPositions = positionHistoryDao.getAllTrails(startMs).first()
            .filter { it.timestampMs <= endMs }
            .sortedBy { it.timestampMs }

        _state.value = ReplayState(
            startTimeMs = startMs,
            endTimeMs = endMs,
            currentTimeMs = startMs
        )
    }

    /**
     * Start or resume playback.
     */
    suspend fun play() {
        if (isRunning) return
        isRunning = true

        _state.value = _state.value.copy(isPlaying = true)
        val speed = _state.value.speed
        var currentTime = _state.value.currentTimeMs
        val endTime = _state.value.endTimeMs

        while (isRunning && currentTime < endTime) {
            // Find positions at current time
            val snapshot = allPositions
                .filter { it.timestampMs <= currentTime }
                .groupBy { it.nodeId }
                .mapValues { (_, positions) -> positions.last() }

            _state.value = _state.value.copy(
                currentTimeMs = currentTime,
                currentPositions = snapshot
            )

            delay((100 / speed).toLong()) // ~10fps base
            currentTime += (1000 * speed).toLong() // advance 1 real-second per tick
        }

        isRunning = false
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun pause() {
        isRunning = false
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun setSpeed(speed: Float) {
        _state.value = _state.value.copy(speed = speed.coerceIn(0.25f, 16.0f))
    }

    fun seekTo(timeMs: Long) {
        _state.value = _state.value.copy(
            currentTimeMs = timeMs.coerceIn(_state.value.startTimeMs, _state.value.endTimeMs)
        )
    }

    fun reset() {
        isRunning = false
        _state.value = ReplayState()
        allPositions = emptyList()
    }
}
