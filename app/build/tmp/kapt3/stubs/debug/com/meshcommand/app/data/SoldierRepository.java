package com.meshcommand.app.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 /2\u00020\u0001:\u0001/B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u000e\u0010\u001e\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010\u001fJ\u0018\u0010 \u001a\u00020\u001a2\b\b\u0002\u0010!\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u001a\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\t0\b2\u0006\u0010#\u001a\u00020\u0010J\u0016\u0010$\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\b2\u0006\u0010#\u001a\u00020\u0010J4\u0010%\u001a\u00020\u001a2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\'2\b\b\u0002\u0010)\u001a\u00020\u00102\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u0010H\u0086@\u00a2\u0006\u0002\u0010*J\u0016\u0010+\u001a\u00020\u001a2\u0006\u0010,\u001a\u00020-H\u0086@\u00a2\u0006\u0002\u0010.R\u001d\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\fR\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\fR\u001d\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\f\u00a8\u00060"}, d2 = {"Lcom/meshcommand/app/data/SoldierRepository;", "", "soldierDao", "Lcom/meshcommand/app/data/dao/SoldierDao;", "eventDao", "Lcom/meshcommand/app/data/dao/EventDao;", "(Lcom/meshcommand/app/data/dao/SoldierDao;Lcom/meshcommand/app/data/dao/EventDao;)V", "allSoldiers", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/meshcommand/app/data/entity/SoldierEntity;", "getAllSoldiers", "()Lkotlinx/coroutines/flow/Flow;", "criticalSoldiers", "getCriticalSoldiers", "onlineCount", "", "getOnlineCount", "onlineSoldiers", "getOnlineSoldiers", "recentEvents", "Lcom/meshcommand/app/data/entity/EventEntity;", "getRecentEvents", "unacknowledgedAlerts", "getUnacknowledgedAlerts", "acknowledgeAlert", "", "eventId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkTimeouts", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "cleanupOldEvents", "maxAgeMs", "getEventsForNode", "nodeId", "getSoldierById", "logEvent", "eventType", "", "message", "severity", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Integer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "processPacket", "packet", "Lcom/meshcommand/app/comm/SoldierPacket;", "(Lcom/meshcommand/app/comm/SoldierPacket;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class SoldierRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.data.dao.SoldierDao soldierDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.data.dao.EventDao eventDao = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "SoldierRepository";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> allSoldiers = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> onlineSoldiers = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.lang.Integer> onlineCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> criticalSoldiers = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> recentEvents = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> unacknowledgedAlerts = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.data.SoldierRepository.Companion Companion = null;
    
    @javax.inject.Inject()
    public SoldierRepository(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.dao.SoldierDao soldierDao, @org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.dao.EventDao eventDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getAllSoldiers() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getOnlineSoldiers() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getOnlineCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getCriticalSoldiers() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getRecentEvents() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getUnacknowledgedAlerts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.meshcommand.app.data.entity.SoldierEntity> getSoldierById(int nodeId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getEventsForNode(int nodeId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processPacket(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.comm.SoldierPacket packet, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object checkTimeouts(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object logEvent(@org.jetbrains.annotations.NotNull()
    java.lang.String eventType, @org.jetbrains.annotations.NotNull()
    java.lang.String message, int severity, @org.jetbrains.annotations.Nullable()
    java.lang.Integer nodeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object acknowledgeAlert(long eventId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object cleanupOldEvents(long maxAgeMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/meshcommand/app/data/SoldierRepository$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}