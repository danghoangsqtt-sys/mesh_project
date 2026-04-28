package com.meshcommand.app.service;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0007\b\u0007\u0018\u0000 \'2\u00020\u0001:\u0002\'(B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\u0012\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0016J\b\u0010\u001f\u001a\u00020\u001aH\u0016J\b\u0010 \u001a\u00020\u001aH\u0016J\"\u0010!\u001a\u00020\"2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\u0006\u0010#\u001a\u00020\"2\u0006\u0010$\u001a\u00020\"H\u0016J\b\u0010%\u001a\u00020\u001aH\u0002J\u0010\u0010&\u001a\u00020\u001a2\u0006\u0010\u0017\u001a\u00020\u0018H\u0002R \u0010\u0003\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0012\u0010\t\u001a\u00060\nR\u00020\u0000X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/meshcommand/app/service/MeshForegroundService;", "Landroid/app/Service;", "()V", "_gatewayGpsFlow", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lkotlin/Pair;", "", "_packetFlow", "Lcom/meshcommand/app/comm/SoldierPacket;", "binder", "Lcom/meshcommand/app/service/MeshForegroundService$LocalBinder;", "frameExtractor", "Lcom/meshcommand/app/comm/FrameExtractor;", "serialCollectionJob", "Lkotlinx/coroutines/Job;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "usbReceiver", "Landroid/content/BroadcastReceiver;", "usbSerialManager", "Lcom/meshcommand/app/comm/UsbSerialManager;", "buildNotification", "Landroid/app/Notification;", "state", "Lcom/meshcommand/app/comm/ConnectionState;", "createNotificationChannel", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "startSerialCollection", "updateNotification", "Companion", "LocalBinder", "app_debug"})
public final class MeshForegroundService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "MeshForegroundService";
    private static final int NOTIFICATION_ID = 1001;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "mesh_command_service";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_NAME = "Mesh Command Service";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_START = "com.meshcommand.app.action.START_SERVICE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "com.meshcommand.app.action.STOP_SERVICE";
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.service.MeshForegroundService.LocalBinder binder = null;
    private com.meshcommand.app.comm.UsbSerialManager usbSerialManager;
    private com.meshcommand.app.comm.FrameExtractor frameExtractor;
    @org.jetbrains.annotations.Nullable()
    private android.content.BroadcastReceiver usbReceiver;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job serialCollectionJob;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.meshcommand.app.comm.SoldierPacket> _packetFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> _gatewayGpsFlow = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.service.MeshForegroundService.Companion Companion = null;
    
    public MeshForegroundService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    private final void startSerialCollection() {
    }
    
    private final void createNotificationChannel() {
    }
    
    private final android.app.Notification buildNotification(com.meshcommand.app.comm.ConnectionState state) {
        return null;
    }
    
    private final void updateNotification(com.meshcommand.app.comm.ConnectionState state) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/meshcommand/app/service/MeshForegroundService$Companion;", "", "()V", "ACTION_START", "", "ACTION_STOP", "CHANNEL_ID", "CHANNEL_NAME", "NOTIFICATION_ID", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0012\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0011\u001a\u00020\u0012J\u0006\u0010\u0013\u001a\u00020\u0012J\u0006\u0010\u0014\u001a\u00020\u0012J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R#\u0010\b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0\n0\t8F\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\t8F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\r\u00a8\u0006\u0019"}, d2 = {"Lcom/meshcommand/app/service/MeshForegroundService$LocalBinder;", "Landroid/os/Binder;", "(Lcom/meshcommand/app/service/MeshForegroundService;)V", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/meshcommand/app/comm/ConnectionState;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "gatewayGpsFlow", "Lkotlinx/coroutines/flow/SharedFlow;", "Lkotlin/Pair;", "", "getGatewayGpsFlow", "()Lkotlinx/coroutines/flow/SharedFlow;", "packetFlow", "Lcom/meshcommand/app/comm/SoldierPacket;", "getPacketFlow", "connect", "", "disconnect", "enableMockMode", "sendCommand", "", "data", "", "app_debug"})
    public final class LocalBinder extends android.os.Binder {
        
        public LocalBinder() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.SharedFlow<com.meshcommand.app.comm.SoldierPacket> getPacketFlow() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.SharedFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> getGatewayGpsFlow() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.comm.ConnectionState> getConnectionState() {
            return null;
        }
        
        public final void connect() {
        }
        
        public final void disconnect() {
        }
        
        public final boolean sendCommand(@org.jetbrains.annotations.NotNull()
        byte[] data) {
            return false;
        }
        
        public final void enableMockMode() {
        }
    }
}