package com.meshcommand.app.data.entity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u001c\b\u0087\b\u0018\u00002\u00020\u0001BI\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\bH\u00c6\u0003J\u0010\u0010\u001f\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0016J\t\u0010 \u001a\u00020\u0006H\u00c6\u0003J\t\u0010!\u001a\u00020\fH\u00c6\u0003JV\u0010\"\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\n\u001a\u00020\u00062\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001\u00a2\u0006\u0002\u0010#J\u0013\u0010$\u001a\u00020\f2\b\u0010%\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010&\u001a\u00020\bH\u00d6\u0001J\t\u0010\'\u001a\u00020\u0006H\u00d6\u0001R\u0016\u0010\u000b\u001a\u00020\f8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0016\u0010\u0005\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0016\u0010\n\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u001a\u0010\t\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0017\u001a\u0004\b\u0015\u0010\u0016R\u0016\u0010\u0007\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0013\u00a8\u0006("}, d2 = {"Lcom/meshcommand/app/data/entity/EventEntity;", "", "id", "", "timestampMs", "eventType", "", "severity", "", "nodeId", "message", "acknowledged", "", "(JJLjava/lang/String;ILjava/lang/Integer;Ljava/lang/String;Z)V", "getAcknowledged", "()Z", "getEventType", "()Ljava/lang/String;", "getId", "()J", "getMessage", "getNodeId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getSeverity", "()I", "getTimestampMs", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "(JJLjava/lang/String;ILjava/lang/Integer;Ljava/lang/String;Z)Lcom/meshcommand/app/data/entity/EventEntity;", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.room.Entity(tableName = "events")
public final class EventEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    @androidx.room.ColumnInfo(name = "timestamp_ms")
    private final long timestampMs = 0L;
    @androidx.room.ColumnInfo(name = "event_type")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String eventType = null;
    @androidx.room.ColumnInfo(name = "severity")
    private final int severity = 0;
    @androidx.room.ColumnInfo(name = "node_id")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer nodeId = null;
    @androidx.room.ColumnInfo(name = "message")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String message = null;
    @androidx.room.ColumnInfo(name = "acknowledged")
    private final boolean acknowledged = false;
    
    public EventEntity(long id, long timestampMs, @org.jetbrains.annotations.NotNull()
    java.lang.String eventType, int severity, @org.jetbrains.annotations.Nullable()
    java.lang.Integer nodeId, @org.jetbrains.annotations.NotNull()
    java.lang.String message, boolean acknowledged) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    public final long getTimestampMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEventType() {
        return null;
    }
    
    public final int getSeverity() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getNodeId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMessage() {
        return null;
    }
    
    public final boolean getAcknowledged() {
        return false;
    }
    
    public final long component1() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    public final boolean component7() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.meshcommand.app.data.entity.EventEntity copy(long id, long timestampMs, @org.jetbrains.annotations.NotNull()
    java.lang.String eventType, int severity, @org.jetbrains.annotations.Nullable()
    java.lang.Integer nodeId, @org.jetbrains.annotations.NotNull()
    java.lang.String message, boolean acknowledged) {
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