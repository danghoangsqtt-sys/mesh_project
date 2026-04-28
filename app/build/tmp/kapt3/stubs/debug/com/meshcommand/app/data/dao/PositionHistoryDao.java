package com.meshcommand.app.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u000b\u001a\u00020\u0005H\'J\u000e\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ$\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u000b\u001a\u00020\u0005H\'J\u0016\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0014"}, d2 = {"Lcom/meshcommand/app/data/dao/PositionHistoryDao;", "", "deleteOlderThan", "", "thresholdMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllTrails", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/meshcommand/app/data/entity/PositionHistoryEntity;", "sinceMs", "getCount", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTrailForNode", "nodeId", "insert", "position", "(Lcom/meshcommand/app/data/entity/PositionHistoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface PositionHistoryDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.entity.PositionHistoryEntity position, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM position_history WHERE node_id = :nodeId AND timestamp_ms >= :sinceMs ORDER BY timestamp_ms ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.PositionHistoryEntity>> getTrailForNode(int nodeId, long sinceMs);
    
    @androidx.room.Query(value = "SELECT * FROM position_history WHERE timestamp_ms >= :sinceMs ORDER BY node_id, timestamp_ms ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.PositionHistoryEntity>> getAllTrails(long sinceMs);
    
    @androidx.room.Query(value = "DELETE FROM position_history WHERE timestamp_ms < :thresholdMs")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOlderThan(long thresholdMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM position_history")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
}