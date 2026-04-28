package com.meshcommand.app.comm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors

class UsbSerialManager(private val context: Context) {

    companion object {
        private const val TAG = "UsbSerialManager"
        private const val ACTION_USB_PERMISSION = "com.meshcommand.app.USB_PERMISSION"
        private const val BAUD_RATE = 115200
        private const val READ_TIMEOUT_MS = 100
        private const val WRITE_TIMEOUT_MS = 1000
        private const val RECONNECT_DELAY_MS = 2000L

        // Known ESP32 T-Beam USB chip VID/PID pairs
        private val KNOWN_DEVICES = listOf(
            Pair(0x10C4, 0xEA60), // Silicon Labs CP210x
            Pair(0x1A86, 0x7523)  // WCH CH340
        )
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialPort: UsbSerialPort? = null
    private var usbConnection: UsbDeviceConnection? = null
    private var ioManager: SerialInputOutputManager? = null
    private var isMockMode = false

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ─────────────────────────────────────────────
    // Device Discovery
    // ─────────────────────────────────────────────

    fun findDevice(): UsbDevice? {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            Log.d(TAG, "No USB serial drivers found")
            return null
        }

        // Prefer known ESP32 devices
        for (driver in availableDrivers) {
            val device = driver.device
            val vid = device.vendorId
            val pid = device.productId
            if (KNOWN_DEVICES.any { it.first == vid && it.second == pid }) {
                Log.i(TAG, "Found known ESP32 device: ${device.deviceName} (VID=$vid, PID=$pid)")
                return device
            }
        }

        // Fall back to first available driver
        val firstDevice = availableDrivers[0].device
        Log.i(TAG, "Using first available USB serial device: ${firstDevice.deviceName}")
        return firstDevice
    }

    // ─────────────────────────────────────────────
    // Connection Management
    // ─────────────────────────────────────────────

    fun connect(device: UsbDevice? = null): Boolean {
        val targetDevice = device ?: findDevice()
        if (targetDevice == null) {
            _connectionState.value = ConnectionState.Error("No USB serial device found")
            return false
        }

        _connectionState.value = ConnectionState.Connecting

        // Check permission
        if (!usbManager.hasPermission(targetDevice)) {
            requestPermission(targetDevice)
            return false // Will retry after permission grant
        }

        return openConnection(targetDevice)
    }

    private fun openConnection(device: UsbDevice): Boolean {
        try {
            val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
            val driver = drivers.firstOrNull { it.device == device }
            if (driver == null) {
                _connectionState.value = ConnectionState.Error("No driver for device")
                return false
            }

            usbConnection = usbManager.openDevice(device)
            if (usbConnection == null) {
                _connectionState.value = ConnectionState.Error("Failed to open USB connection")
                return false
            }

            serialPort = driver.ports[0]
            serialPort?.open(usbConnection)
            serialPort?.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            Log.i(TAG, "USB Serial connected: ${device.deviceName} @ $BAUD_RATE baud")
            _connectionState.value = ConnectionState.Connected(device.deviceName)
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
            cleanup()
            return false
        }
    }

    fun disconnect() {
        Log.i(TAG, "Disconnecting USB Serial")
        cleanup()
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun cleanup() {
        try {
            ioManager?.listener = null
            ioManager?.stop()
            ioManager = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping IO manager: ${e.message}")
        }
        try {
            serialPort?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing serial port: ${e.message}")
        }
        try {
            usbConnection?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing USB connection: ${e.message}")
        }
        serialPort = null
        usbConnection = null
    }

    // ─────────────────────────────────────────────
    // Serial Data Flow (callbackFlow bridge)
    // ─────────────────────────────────────────────

    fun serialDataFlow(): Flow<ByteArray> = callbackFlow {
        val port = serialPort
        if (port == null) {
            close(IOException("Serial port not connected"))
            return@callbackFlow
        }

        val listener = object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                trySend(data)
            }

            override fun onRunError(e: Exception) {
                Log.e(TAG, "Serial read error: ${e.message}")
                _connectionState.value = ConnectionState.Error("Read error: ${e.message}")
                close(e)
            }
        }

        val manager = SerialInputOutputManager(port, listener)
        manager.readTimeout = READ_TIMEOUT_MS
        ioManager = manager
        Executors.newSingleThreadExecutor().submit(manager)

        Log.i(TAG, "Serial data flow started")

        awaitClose {
            Log.i(TAG, "Serial data flow closing")
            manager.listener = null
            manager.stop()
            ioManager = null
        }
    }

    // ─────────────────────────────────────────────
    // Write Data
    // ─────────────────────────────────────────────

    fun write(data: ByteArray): Boolean {
        val port = serialPort ?: return false
        return try {
            port.write(data, WRITE_TIMEOUT_MS)
            Log.d(TAG, "Wrote ${data.size} bytes to serial")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Write failed: ${e.message}")
            false
        }
    }

    // ─────────────────────────────────────────────
    // USB Permission Handling
    // ─────────────────────────────────────────────

    private fun requestPermission(device: UsbDevice) {
        Log.d(TAG, "Requesting USB permission for ${device.deviceName}")
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), flags
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    // ─────────────────────────────────────────────
    // USB Attach/Detach BroadcastReceiver
    // ─────────────────────────────────────────────

    fun createUsbReceiver(scope: CoroutineScope): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    ACTION_USB_PERMISSION -> {
                        val granted = intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false
                        )
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                        if (granted && device != null) {
                            Log.i(TAG, "USB permission granted for ${device.deviceName}")
                            openConnection(device)
                        } else {
                            Log.w(TAG, "USB permission denied")
                            _connectionState.value = ConnectionState.Error("USB permission denied")
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        Log.i(TAG, "USB device attached")
                        scope.launch(Dispatchers.IO) {
                            delay(500) // Allow device to initialize
                            connect()
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        Log.i(TAG, "USB device detached")
                        cleanup()
                        _connectionState.value = ConnectionState.Disconnected
                        // Auto-reconnect will happen on next attach
                    }
                }
            }
        }
    }

    fun getIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
    }

    // ─────────────────────────────────────────────
    // Mock Mode (Development without hardware)
    // ─────────────────────────────────────────────

    fun enableMockMode() {
        isMockMode = true
        _connectionState.value = ConnectionState.Connected("MOCK_DEVICE")
        Log.i(TAG, "Mock mode enabled")
    }

    fun isMockMode(): Boolean = isMockMode
}
