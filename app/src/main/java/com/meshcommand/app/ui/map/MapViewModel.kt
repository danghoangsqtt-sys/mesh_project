package com.meshcommand.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshcommand.app.data.SoldierRepository
import com.meshcommand.app.data.entity.SoldierEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val soldierRepository: SoldierRepository
) : ViewModel() {

    val soldierPositions: StateFlow<List<SoldierEntity>> = soldierRepository.allSoldiers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _gatewayPosition = MutableStateFlow<Pair<Double, Double>?>(null)
    val gatewayPosition: StateFlow<Pair<Double, Double>?> = _gatewayPosition.asStateFlow()

    private val _selectedSoldierId = MutableStateFlow<Int?>(null)
    val selectedSoldierId: StateFlow<Int?> = _selectedSoldierId.asStateFlow()

    // Camera target for centering on a soldier
    private val _cameraTarget = MutableStateFlow<Pair<Double, Double>?>(null)
    val cameraTarget: StateFlow<Pair<Double, Double>?> = _cameraTarget.asStateFlow()

    fun selectSoldier(nodeId: Int?) {
        _selectedSoldierId.value = nodeId
    }

    fun centerOnSoldier(nodeId: Int) {
        val soldier = soldierPositions.value.find { it.nodeId == nodeId }
        if (soldier != null && soldier.latitude != 0.0 && soldier.longitude != 0.0) {
            _cameraTarget.value = soldier.latitude to soldier.longitude
            _selectedSoldierId.value = nodeId
        }
    }

    fun updateGatewayPosition(lat: Double, lon: Double) {
        _gatewayPosition.value = lat to lon
    }

    fun clearCameraTarget() {
        _cameraTarget.value = null
    }
}
