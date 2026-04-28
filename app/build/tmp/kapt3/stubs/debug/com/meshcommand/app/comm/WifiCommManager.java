package com.meshcommand.app.comm;

/**
 * WiFi TCP socket client for connecting to Gateway's AP.
 * Receives the same 40-byte binary protocol packets over TCP.
 *
 * Default Gateway AP config:
 * - SSID: MeshGateway_XXX
 * - IP:   192.168.4.1
 * - Port: 8888
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\b\u0007\u0018\u0000 *2\u00020\u0001:\u0001*B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001e\u001a\u00020\u001fH\u0002J\"\u0010 \u001a\u00020\u001f2\b\b\u0002\u0010!\u001a\u00020\"2\b\b\u0002\u0010#\u001a\u00020$H\u0086@\u00a2\u0006\u0002\u0010%J,\u0010&\u001a\u00020\u001f2\b\b\u0002\u0010!\u001a\u00020\"2\b\b\u0002\u0010#\u001a\u00020$2\b\b\u0002\u0010\'\u001a\u00020$H\u0086@\u00a2\u0006\u0002\u0010(J\u0006\u0010)\u001a\u00020\u001fR\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0006\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u0012\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0016\u001a\u00020\u00178F\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0015R\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/meshcommand/app/comm/WifiCommManager;", "", "()V", "_connectionState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/meshcommand/app/comm/ConnectionState;", "_gatewayGpsFlow", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lkotlin/Pair;", "", "_packetFlow", "Lcom/meshcommand/app/comm/SoldierPacket;", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "frameExtractor", "Lcom/meshcommand/app/comm/FrameExtractor;", "gatewayGpsFlow", "Lkotlinx/coroutines/flow/SharedFlow;", "getGatewayGpsFlow", "()Lkotlinx/coroutines/flow/SharedFlow;", "isConnected", "", "()Z", "isRunning", "packetFlow", "getPacketFlow", "socket", "Ljava/net/Socket;", "closeSocket", "", "connect", "host", "", "port", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "connectWithRetry", "maxRetries", "(Ljava/lang/String;IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "disconnect", "Companion", "app_debug"})
public final class WifiCommManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "WifiCommManager";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_HOST = "192.168.4.1";
    public static final int DEFAULT_PORT = 8888;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_BUFFER_SIZE = 1024;
    private static final long RECONNECT_DELAY_MS = 3000L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.meshcommand.app.comm.ConnectionState> _connectionState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.comm.ConnectionState> connectionState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.meshcommand.app.comm.SoldierPacket> _packetFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.meshcommand.app.comm.SoldierPacket> packetFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> _gatewayGpsFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> gatewayGpsFlow = null;
    @org.jetbrains.annotations.Nullable()
    private java.net.Socket socket;
    @kotlin.jvm.Volatile()
    private volatile boolean isRunning = false;
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.comm.FrameExtractor frameExtractor = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.comm.WifiCommManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public WifiCommManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.comm.ConnectionState> getConnectionState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.meshcommand.app.comm.SoldierPacket> getPacketFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> getGatewayGpsFlow() {
        return null;
    }
    
    /**
     * Connect to the Gateway's TCP server and start reading data.
     * This suspends and runs the read loop until disconnected.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object connect(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Connect with auto-reconnect on failure.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object connectWithRetry(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, int maxRetries, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void disconnect() {
    }
    
    public final boolean isConnected() {
        return false;
    }
    
    private final void closeSocket() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/meshcommand/app/comm/WifiCommManager$Companion;", "", "()V", "CONNECT_TIMEOUT_MS", "", "DEFAULT_HOST", "", "DEFAULT_PORT", "READ_BUFFER_SIZE", "RECONNECT_DELAY_MS", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}