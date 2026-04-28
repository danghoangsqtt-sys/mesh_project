package com.meshcommand.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.meshcommand.app.MainActivity
import com.meshcommand.app.comm.ConnectionState
import com.meshcommand.app.comm.FrameExtractor
import com.meshcommand.app.comm.SoldierPacket
import com.meshcommand.app.comm.UsbSerialManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeshForegroundService : Service() {

    companion object {
        private const val TAG = "MeshForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "mesh_command_service"
        private const val CHANNEL_NAME = "Mesh Command Service"

        const val ACTION_START = "com.meshcommand.app.action.START_SERVICE"
        const val ACTION_STOP = "com.meshcommand.app.action.STOP_SERVICE"
    }

    // ─────────────────────────────────────────────
    // Service Binder
    // ─────────────────────────────────────────────

    inner class LocalBinder : Binder() {
        val packetFlow: SharedFlow<SoldierPacket> get() = _packetFlow.asSharedFlow()
        val gatewayGpsFlow: SharedFlow<Pair<Double, Double>> get() = _gatewayGpsFlow.asSharedFlow()
        val connectionState: StateFlow<ConnectionState> get() = usbSerialManager.connectionState

        fun connect() {
            serviceScope.launch(Dispatchers.IO) {
                usbSerialManager.connect()
                startSerialCollection()
            }
        }

        fun disconnect() {
            serialCollectionJob?.cancel()
            usbSerialManager.disconnect()
            updateNotification(ConnectionState.Disconnected)
        }

        fun sendCommand(data: ByteArray): Boolean {
            return usbSerialManager.write(data)
        }

        fun enableMockMode() {
            usbSerialManager.enableMockMode()
        }
    }

    private val binder = LocalBinder()

    // ─────────────────────────────────────────────
    // Internal State
    // ─────────────────────────────────────────────

    private lateinit var usbSerialManager: UsbSerialManager
    private lateinit var frameExtractor: FrameExtractor
    private var usbReceiver: BroadcastReceiver? = null
    private var serialCollectionJob: Job? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _packetFlow = MutableSharedFlow<SoldierPacket>(
        replay = 0,
        extraBufferCapacity = 64
    )
    private val _gatewayGpsFlow = MutableSharedFlow<Pair<Double, Double>>(
        replay = 1,
        extraBufferCapacity = 8
    )

    // ─────────────────────────────────────────────
    // Service Lifecycle
    // ─────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service onCreate")

        createNotificationChannel()

        usbSerialManager = UsbSerialManager(this)

        frameExtractor = FrameExtractor(
            onPacketReceived = { packet ->
                _packetFlow.tryEmit(packet)
            },
            onGatewayGpsReceived = { lat, lon ->
                _gatewayGpsFlow.tryEmit(lat to lon)
            }
        )

        // Register USB attach/detach receiver
        usbReceiver = usbSerialManager.createUsbReceiver(serviceScope)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, usbSerialManager.getIntentFilter(), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, usbSerialManager.getIntentFilter())
        }

        // Observe connection state changes → update notification
        serviceScope.launch {
            usbSerialManager.connectionState.collect { state ->
                updateNotification(state)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Start foreground
        val notification = buildNotification(ConnectionState.Disconnected)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Auto-connect to USB device
        serviceScope.launch(Dispatchers.IO) {
            val connected = usbSerialManager.connect()
            if (connected) {
                startSerialCollection()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "Service onBind")
        return binder
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")
        serialCollectionJob?.cancel()
        usbSerialManager.disconnect()

        usbReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering receiver: ${e.message}")
            }
        }

        serviceScope.cancel()
        super.onDestroy()
    }

    // ─────────────────────────────────────────────
    // Serial Data Collection
    // ─────────────────────────────────────────────

    private fun startSerialCollection() {
        serialCollectionJob?.cancel()
        serialCollectionJob = serviceScope.launch {
            usbSerialManager.serialDataFlow()
                .catch { e ->
                    Log.e(TAG, "Serial data flow error: ${e.message}")
                }
                .collect { bytes ->
                    frameExtractor.append(bytes)
                }
        }
        Log.i(TAG, "Serial data collection started")
    }

    // ─────────────────────────────────────────────
    // Notification Management
    // ─────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mesh Command serial connection status"
            setShowBadge(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(state: ConnectionState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MeshForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text, icon) = when (state) {
            is ConnectionState.Connected -> Triple(
                "Mesh Command — Connected",
                "Connected to ${state.deviceName}",
                android.R.drawable.stat_sys_data_bluetooth
            )
            is ConnectionState.Connecting -> Triple(
                "Mesh Command — Connecting...",
                "Searching for gateway device...",
                android.R.drawable.stat_notify_sync
            )
            is ConnectionState.Error -> Triple(
                "Mesh Command — Error",
                state.message,
                android.R.drawable.stat_notify_error
            )
            is ConnectionState.Disconnected -> Triple(
                "Mesh Command — Disconnected",
                "No USB device connected",
                android.R.drawable.stat_sys_warning
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(icon)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopIntent
            )
            .build()
    }

    private fun updateNotification(state: ConnectionState) {
        try {
            val notification = buildNotification(state)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update notification: ${e.message}")
        }
    }
}
