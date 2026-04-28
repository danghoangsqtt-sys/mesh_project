package com.meshcommand.app.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J&\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\n2\u0006\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u000eH\'J\u001e\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\n2\b\b\u0002\u0010\u000f\u001a\u00020\u000eH\'J\u0014\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nH\'J\u0016\u0010\u0012\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0014\u00a8\u0006\u0015"}, d2 = {"Lcom/meshcommand/app/data/dao/EventDao;", "", "acknowledgeEvent", "", "eventId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteOlderThan", "thresholdMs", "getEventsForNode", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/meshcommand/app/data/entity/EventEntity;", "nodeId", "", "limit", "getRecentEvents", "getUnacknowledgedAlerts", "insert", "event", "(Lcom/meshcommand/app/data/entity/EventEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface EventDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.entity.EventEntity event, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM events ORDER BY timestamp_ms DESC LIMIT :limit")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getRecentEvents(int limit);
    
    @androidx.room.Query(value = "SELECT * FROM events WHERE node_id = :nodeId ORDER BY timestamp_ms DESC LIMIT :limit")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getEventsForNode(int nodeId, int limit);
    
    @androidx.room.Query(value = "SELECT * FROM events WHERE event_type = \'ALERT\' AND acknowledged = 0 ORDER BY timestamp_ms DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getUnacknowledgedAlerts();
    
    @androidx.room.Query(value = "UPDATE events SET acknowledged = 1 WHERE id = :eventId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object acknowledgeEvent(long eventId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM events WHERE timestamp_ms < :thresholdMs")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOlderThan(long thresholdMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}