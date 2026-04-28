package com.meshcommand.app.ui.tactical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshcommand.app.data.SoldierRepository
import com.meshcommand.app.data.entity.EventEntity
import com.meshcommand.app.data.entity.SoldierEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TacticalViewModel @Inject constructor(
    private val soldierRepository: SoldierRepository
) : ViewModel() {

    val soldiers: StateFlow<List<SoldierEntity>> = soldierRepository.allSoldiers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val onlineCount: StateFlow<Int> = soldierRepository.onlineCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentEvents: StateFlow<List<EventEntity>> = soldierRepository.recentEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNodeId = MutableStateFlow<Int?>(null)
    val selectedNodeId: StateFlow<Int?> = _selectedNodeId.asStateFlow()

    private val _lastRxTime = MutableStateFlow<Long>(0L)
    val lastRxTime: StateFlow<Long> = _lastRxTime.asStateFlow()

    fun selectSoldier(nodeId: Int) {
        _selectedNodeId.value = nodeId
    }

    fun updateLastRxTime() {
        _lastRxTime.value = System.currentTimeMillis()
    }

    fun acknowledgeAlert(eventId: Long) {
        viewModelScope.launch {
            soldierRepository.acknowledgeAlert(eventId)
        }
    }

    fun logEvent(eventType: String, message: String, severity: Int = 0, nodeId: Int? = null) {
        viewModelScope.launch {
            soldierRepository.logEvent(eventType, message, severity, nodeId)
        }
    }
}
