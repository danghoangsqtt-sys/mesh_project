package com.meshcommand.app.comm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0002\b\u0004\u0018\u0000 +2\u00020\u0001:\u0001+B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0016\u001a\u00020\u0017H\u0002J\u0012\u0010\u0018\u001a\u00020\u000f2\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001aJ\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001eJ\u0006\u0010\u001f\u001a\u00020\u0017J\u0006\u0010 \u001a\u00020\u0017J\b\u0010!\u001a\u0004\u0018\u00010\u001aJ\u0006\u0010\"\u001a\u00020#J\u0006\u0010\u000e\u001a\u00020\u000fJ\u0010\u0010$\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0010\u0010%\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\f\u0010&\u001a\b\u0012\u0004\u0012\u00020(0\'J\u000e\u0010)\u001a\u00020\u000f2\u0006\u0010*\u001a\u00020(R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/meshcommand/app/comm/UsbSerialManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_connectionState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/meshcommand/app/comm/ConnectionState;", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "ioManager", "Lcom/hoho/android/usbserial/util/SerialInputOutputManager;", "isMockMode", "", "serialPort", "Lcom/hoho/android/usbserial/driver/UsbSerialPort;", "usbConnection", "Landroid/hardware/usb/UsbDeviceConnection;", "usbManager", "Landroid/hardware/usb/UsbManager;", "cleanup", "", "connect", "device", "Landroid/hardware/usb/UsbDevice;", "createUsbReceiver", "Landroid/content/BroadcastReceiver;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "disconnect", "enableMockMode", "findDevice", "getIntentFilter", "Landroid/content/IntentFilter;", "openConnection", "requestPermission", "serialDataFlow", "Lkotlinx/coroutines/flow/Flow;", "", "write", "data", "Companion", "app_debug"})
public final class UsbSerialManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "UsbSerialManager";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ACTION_USB_PERMISSION = "com.meshcommand.app.USB_PERMISSION";
    private static final int BAUD_RATE = 115200;
    private static final int READ_TIMEOUT_MS = 100;
    private static final int WRITE_TIMEOUT_MS = 1000;
    private static final long RECONNECT_DELAY_MS = 2000L;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> KNOWN_DEVICES = null;
    @org.jetbrains.annotations.NotNull()
    private final android.hardware.usb.UsbManager usbManager = null;
    @org.jetbrains.annotations.Nullable()
    private com.hoho.android.usbserial.driver.UsbSerialPort serialPort;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.usb.UsbDeviceConnection usbConnection;
    @org.jetbrains.annotations.Nullable()
    private com.hoho.android.usbserial.util.SerialInputOutputManager ioManager;
    private boolean isMockMode = false;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.meshcommand.app.comm.ConnectionState> _connectionState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.comm.ConnectionState> connectionState = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.comm.UsbSerialManager.Companion Companion = null;
    
    public UsbSerialManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.comm.ConnectionState> getConnectionState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.hardware.usb.UsbDevice findDevice() {
        return null;
    }
    
    public final boolean connect(@org.jetbrains.annotations.Nullable()
    android.hardware.usb.UsbDevice device) {
        return false;
    }
    
    private final boolean openConnection(android.hardware.usb.UsbDevice device) {
        return false;
    }
    
    public final void disconnect() {
    }
    
    private final void cleanup() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<byte[]> serialDataFlow() {
        return null;
    }
    
    public final boolean write(@org.jetbrains.annotations.NotNull()
    byte[] data) {
        return false;
    }
    
    private final void requestPermission(android.hardware.usb.UsbDevice device) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.BroadcastReceiver createUsbReceiver(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.CoroutineScope scope) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.IntentFilter getIntentFilter() {
        return null;
    }
    
    public final void enableMockMode() {
    }
    
    public final boolean isMockMode() {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R \u0010\u0007\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/meshcommand/app/comm/UsbSerialManager$Companion;", "", "()V", "ACTION_USB_PERMISSION", "", "BAUD_RATE", "", "KNOWN_DEVICES", "", "Lkotlin/Pair;", "READ_TIMEOUT_MS", "RECONNECT_DELAY_MS", "", "TAG", "WRITE_TIMEOUT_MS", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}