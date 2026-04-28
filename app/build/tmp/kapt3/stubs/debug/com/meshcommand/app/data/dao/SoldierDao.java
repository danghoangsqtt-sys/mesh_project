package com.meshcommand.app.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J\u0018\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u00032\u0006\u0010\u0007\u001a\u00020\bH\'J\u0014\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J\u000e\u0010\n\u001a\b\u0012\u0004\u0012\u00020\b0\u0003H\'J\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0014"}, d2 = {"Lcom/meshcommand/app/data/dao/SoldierDao;", "", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/meshcommand/app/data/entity/SoldierEntity;", "getByNodeId", "nodeId", "", "getCriticalSoldiers", "getOnlineCount", "getOnlineSoldiers", "markOffline", "", "thresholdMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "soldier", "(Lcom/meshcommand/app/data/entity/SoldierEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface SoldierDao {
    
    @androidx.room.Upsert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.entity.SoldierEntity soldier, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM soldiers ORDER BY alert_level DESC, node_id ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getAll();
    
    @androidx.room.Query(value = "SELECT * FROM soldiers WHERE node_id = :nodeId")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.meshcommand.app.data.entity.SoldierEntity> getByNodeId(int nodeId);
    
    @androidx.room.Query(value = "SELECT * FROM soldiers WHERE is_online = 1 ORDER BY node_id ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getOnlineSoldiers();
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM soldiers WHERE is_online = 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getOnlineCount();
    
    @androidx.room.Query(value = "UPDATE soldiers SET is_online = 0, alert_level = 0 WHERE last_seen_ms < :thresholdMs AND is_online = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markOffline(long thresholdMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM soldiers WHERE alert_level >= 2 AND is_online = 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getCriticalSoldiers();
}