package com.meshcommand.app.comm;

/**
 * Multi-Gateway Manager — handles simultaneous connections to multiple gateways
 * (USB + WiFi). Merges packet streams and de-duplicates by (nodeId + timestamp).
 *
 * Usage: call feedPacket() from each gateway's collection loop.
 * The manager de-duplicates and forwards to the repository.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 (2\u00020\u0001:\u0002()B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u001c\u001a\u00020\u001dH\u0002J&\u0010\u001e\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\u000e2\u0006\u0010!\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\"J\u0010\u0010#\u001a\u00020$2\u0006\u0010\u001f\u001a\u00020\u000bH\u0002J\u001e\u0010%\u001a\u00020\u001d2\u0006\u0010 \u001a\u00020\u000e2\u0006\u0010!\u001a\u00020\u000e2\u0006\u0010&\u001a\u00020\'R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000f0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u001a\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00190\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/meshcommand/app/comm/MultiGatewayManager;", "", "soldierRepository", "Lcom/meshcommand/app/data/SoldierRepository;", "(Lcom/meshcommand/app/data/SoldierRepository;)V", "_gateways", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/meshcommand/app/comm/MultiGatewayManager$GatewayInfo;", "_mergedPacketFlow", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/meshcommand/app/comm/SoldierPacket;", "deduplicationCache", "Ljava/util/concurrent/ConcurrentHashMap;", "", "", "gateways", "Lkotlinx/coroutines/flow/StateFlow;", "getGateways", "()Lkotlinx/coroutines/flow/StateFlow;", "mergedPacketFlow", "Lkotlinx/coroutines/flow/SharedFlow;", "getMergedPacketFlow", "()Lkotlinx/coroutines/flow/SharedFlow;", "packetCounts", "Ljava/util/concurrent/atomic/AtomicLong;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "cleanupDedupCache", "", "feedPacket", "packet", "gatewayId", "gatewayType", "(Lcom/meshcommand/app/comm/SoldierPacket;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isDuplicate", "", "updateGatewayState", "state", "Lcom/meshcommand/app/comm/ConnectionState;", "Companion", "GatewayInfo", "app_debug"})
public final class MultiGatewayManager {
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.data.SoldierRepository soldierRepository = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "MultiGatewayManager";
    private static final long DEDUP_WINDOW_MS = 2000L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.Long> deduplicationCache = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.meshcommand.app.comm.SoldierPacket> _mergedPacketFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.meshcommand.app.comm.SoldierPacket> mergedPacketFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.meshcommand.app.comm.MultiGatewayManager.GatewayInfo>> _gateways = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.comm.MultiGatewayManager.GatewayInfo>> gateways = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, java.util.concurrent.atomic.AtomicLong> packetCounts = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.comm.MultiGatewayManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public MultiGatewayManager(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.SoldierRepository soldierRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.meshcommand.app.comm.SoldierPacket> getMergedPacketFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.comm.MultiGatewayManager.GatewayInfo>> getGateways() {
        return null;
    }
    
    /**
     * Feed a packet from any gateway source. De-duplicates and forwards to repository.
     * @param gatewayId unique gateway identifier (e.g. "usb-primary", "wifi-192.168.4.1")
     * @param gatewayType "USB" or "WiFi"
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object feedPacket(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.comm.SoldierPacket packet, @org.jetbrains.annotations.NotNull()
    java.lang.String gatewayId, @org.jetbrains.annotations.NotNull()
    java.lang.String gatewayType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void updateGatewayState(@org.jetbrains.annotations.NotNull()
    java.lang.String gatewayId, @org.jetbrains.annotations.NotNull()
    java.lang.String gatewayType, @org.jetbrains.annotations.NotNull()
    com.meshcommand.app.comm.ConnectionState state) {
    }
    
    private final boolean isDuplicate(com.meshcommand.app.comm.SoldierPacket packet) {
        return false;
    }
    
    private final void cleanupDedupCache() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/meshcommand/app/comm/MultiGatewayManager$Companion;", "", "()V", "DEDUP_WINDOW_MS", "", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\bH\u00c6\u0003J1\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000b\u00a8\u0006\u001c"}, d2 = {"Lcom/meshcommand/app/comm/MultiGatewayManager$GatewayInfo;", "", "id", "", "type", "state", "Lcom/meshcommand/app/comm/ConnectionState;", "packetsReceived", "", "(Ljava/lang/String;Ljava/lang/String;Lcom/meshcommand/app/comm/ConnectionState;J)V", "getId", "()Ljava/lang/String;", "getPacketsReceived", "()J", "getState", "()Lcom/meshcommand/app/comm/ConnectionState;", "getType", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class GatewayInfo {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String type = null;
        @org.jetbrains.annotations.NotNull()
        private final com.meshcommand.app.comm.ConnectionState state = null;
        private final long packetsReceived = 0L;
        
        public GatewayInfo(@org.jetbrains.annotations.NotNull()
        java.lang.String id, @org.jetbrains.annotations.NotNull()
        java.lang.String type, @org.jetbrains.annotations.NotNull()
        com.meshcommand.app.comm.ConnectionState state, long packetsReceived) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.meshcommand.app.comm.ConnectionState getState() {
            return null;
        }
        
        public final long getPacketsReceived() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.meshcommand.app.comm.ConnectionState component3() {
            return null;
        }
        
        public final long component4() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.meshcommand.app.comm.MultiGatewayManager.GatewayInfo copy(@org.jetbrains.annotations.NotNull()
        java.lang.String id, @org.jetbrains.annotations.NotNull()
        java.lang.String type, @org.jetbrains.annotations.NotNull()
        com.meshcommand.app.comm.ConnectionState state, long packetsReceived) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}