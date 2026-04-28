package com.meshcommand.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.meshcommand.app.comm.ConnectionState
import com.meshcommand.app.data.SoldierRepository
import com.meshcommand.app.service.MeshForegroundService
import com.meshcommand.app.ui.map.MapScreen
import com.meshcommand.app.ui.map.MapViewModel
import com.meshcommand.app.ui.tactical.TacticalPanel
import com.meshcommand.app.ui.tactical.TacticalViewModel
import com.meshcommand.app.ui.theme.MeshColors
import com.meshcommand.app.ui.theme.MeshCommandTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var soldierRepository: SoldierRepository

    private var serviceBinder: MeshForegroundService.LocalBinder? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Service connected")
            val binder = service as MeshForegroundService.LocalBinder
            serviceBinder = binder
            isBound = true

            // Collect parsed packets → repository
            lifecycleScope.launch(Dispatchers.IO) {
                binder.packetFlow
                    .catch { e -> Log.e(TAG, "Packet flow error: ${e.message}") }
                    .collect { packet ->
                        soldierRepository.processPacket(packet)
                    }
            }

            // Collect gateway GPS
            lifecycleScope.launch(Dispatchers.IO) {
                binder.gatewayGpsFlow
                    .catch { e -> Log.e(TAG, "Gateway GPS flow error: ${e.message}") }
                    .collect { (lat, lon) ->
                        Log.d(TAG, "Gateway GPS: $lat, $lon")
                    }
            }

            // Collect ACKs
            lifecycleScope.launch(Dispatchers.IO) {
                binder.ackFlow
                    .catch { e -> Log.e(TAG, "ACK flow error: ${e.message}") }
                    .collect { ack ->
                        Log.d(TAG, "ACK received from Node ${ack.senderNodeId}")
                        soldierRepository.logEvent(
                            eventType = "COMM_ACK",
                            message = "Command Delivered (CRC: ${ack.crc16})",
                            severity = 0,
                            nodeId = ack.senderNodeId
                        )
                    }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(TAG, "Service disconnected")
            serviceBinder = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        // Start and bind to ForegroundService
        val serviceIntent = Intent(this, MeshForegroundService::class.java).apply {
            action = MeshForegroundService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        bindService(
            Intent(this, MeshForegroundService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        // Periodic timeout check (every 5 seconds)
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                delay(5000)
                soldierRepository.checkTimeouts()
            }
        }

        setContent {
            MeshCommandTheme {
                MeshCommandScreen(
                    onSendCommandClick = { message ->
                        Log.d(TAG, "Send command: $message")
                        serviceBinder?.let { binder ->
                            val targetId = if (message.contains("→Node ")) {
                                message.substringAfter("→Node ").substringBefore("]").toIntOrNull() ?: 0
                            } else 0
                            val packet = com.meshcommand.app.comm.CommandPacket(
                                targetNodeId = targetId,
                                message = message
                            )
                            binder.sendCommand(packet.toByteArray())
                            
                            val targetStr = if (targetId == 0) "Broadcast" else "Node $targetId"
                            // Can't directly access tacticalViewModel here without fetching it, 
                            // but we can log via the lambda
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}

@Composable
fun MeshCommandScreen(onSendCommandClick: (String) -> Unit) {
    val mapViewModel: MapViewModel = hiltViewModel()
    val tacticalViewModel: TacticalViewModel = hiltViewModel()

    // Connection state — default disconnected until service binds
    val connectionState by mapViewModel.soldierPositions.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MeshColors.SurfaceDark)
    ) {
        // Map — 65%
        MapScreen(
            viewModel = mapViewModel,
            modifier = Modifier.weight(0.65f)
        )

        // Tactical Panel — 35%
        TacticalPanel(
            viewModel = tacticalViewModel,
            connectionState = ConnectionState.Disconnected, // Will be wired to service binder in Phase 2
            onConnect = { /* Service auto-connects on start */ },
            onDisconnect = { /* Will be wired to service binder */ },
            onSendCommand = { message ->
                onSendCommandClick(message)
                val targetId = if (message.contains("→Node ")) {
                    message.substringAfter("→Node ").substringBefore("]").toIntOrNull() ?: 0
                } else 0
                val targetStr = if (targetId == 0) "Broadcast" else "Node $targetId"
                tacticalViewModel.logEvent("COMM_TX", "Sent to $targetStr", 0, targetId.takeIf { it > 0 })
            },
            onSoldierClick = { nodeId ->
                mapViewModel.centerOnSoldier(nodeId)
            },
            modifier = Modifier.weight(0.35f)
        )
    }
}
