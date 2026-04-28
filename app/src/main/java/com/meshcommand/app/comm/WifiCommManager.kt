package com.meshcommand.app.comm

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WiFi TCP socket client for connecting to Gateway's AP.
 * Receives the same 40-byte binary protocol packets over TCP.
 *
 * Default Gateway AP config:
 * - SSID: MeshGateway_XXX
 * - IP:   192.168.4.1
 * - Port: 8888
 */
@Singleton
class WifiCommManager @Inject constructor() {

    companion object {
        private const val TAG = "WifiCommManager"
        const val DEFAULT_HOST = "192.168.4.1"
        const val DEFAULT_PORT = 8888
        private const val CONNECT_TIMEOUT_MS = 5000
        private const val READ_BUFFER_SIZE = 1024
        private const val RECONNECT_DELAY_MS = 3000L
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _packetFlow = MutableSharedFlow<SoldierPacket>(extraBufferCapacity = 64)
    val packetFlow: SharedFlow<SoldierPacket> = _packetFlow.asSharedFlow()

    private val _gatewayGpsFlow = MutableSharedFlow<Pair<Double, Double>>(extraBufferCapacity = 16)
    val gatewayGpsFlow: SharedFlow<Pair<Double, Double>> = _gatewayGpsFlow.asSharedFlow()

    private var socket: Socket? = null
    @Volatile
    private var isRunning = false

    private val frameExtractor = FrameExtractor(
        onPacketReceived = { packet ->
            _packetFlow.tryEmit(packet)
        },
        onGatewayGpsReceived = { lat, lon ->
            _gatewayGpsFlow.tryEmit(lat to lon)
        }
    )

    /**
     * Connect to the Gateway's TCP server and start reading data.
     * This suspends and runs the read loop until disconnected.
     */
    suspend fun connect(
        host: String = DEFAULT_HOST,
        port: Int = DEFAULT_PORT
    ) = withContext(Dispatchers.IO) {
        if (isRunning) {
            Log.w(TAG, "Already connected, disconnect first")
            return@withContext
        }

        isRunning = true
        _connectionState.value = ConnectionState.Connecting

        try {
            val sock = Socket()
            sock.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
            sock.soTimeout = 0 // blocking read
            socket = sock

            val deviceName = "WiFi:$host:$port"
            _connectionState.value = ConnectionState.Connected(deviceName)
            Log.i(TAG, "Connected to $deviceName")

            // Read loop
            val buffer = ByteArray(READ_BUFFER_SIZE)
            val inputStream = sock.getInputStream()

            while (isRunning && isActive && !sock.isClosed) {
                try {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) {
                        Log.w(TAG, "End of stream reached")
                        break
                    }
                    if (bytesRead > 0) {
                        val data = buffer.copyOf(bytesRead)
                        frameExtractor.append(data)
                    }
                } catch (e: IOException) {
                    if (isRunning) {
                        Log.e(TAG, "Read error: ${e.message}")
                    }
                    break
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            _connectionState.value = ConnectionState.Error("WiFi: ${e.message}")
        } finally {
            closeSocket()
            if (isRunning) {
                _connectionState.value = ConnectionState.Disconnected
            }
            isRunning = false
        }
    }

    /**
     * Connect with auto-reconnect on failure.
     */
    suspend fun connectWithRetry(
        host: String = DEFAULT_HOST,
        port: Int = DEFAULT_PORT,
        maxRetries: Int = Int.MAX_VALUE
    ) = withContext(Dispatchers.IO) {
        var retryCount = 0
        while (isActive && retryCount < maxRetries) {
            connect(host, port)
            if (!isRunning) break // manually disconnected
            retryCount++
            Log.i(TAG, "Reconnecting in ${RECONNECT_DELAY_MS}ms (attempt $retryCount)")
            delay(RECONNECT_DELAY_MS)
        }
    }

    fun disconnect() {
        Log.i(TAG, "Disconnecting WiFi")
        isRunning = false
        closeSocket()
        _connectionState.value = ConnectionState.Disconnected
    }

    val isConnected: Boolean
        get() = socket?.isConnected == true && !socket!!.isClosed

    private fun closeSocket() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.w(TAG, "Error closing socket: ${e.message}")
        }
        socket = null
    }
}
