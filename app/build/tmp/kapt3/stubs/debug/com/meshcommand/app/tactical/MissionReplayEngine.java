package com.meshcommand.app.tactical;

/**
 * Mission Replay engine — plays back recorded position history
 * with configurable speed control and timeline scrubbing.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0010\u0007\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001 B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0006\u0010\u0017\u001a\u00020\u0012J\u000e\u0010\u0018\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u0006\u0010\u001a\u001a\u00020\u0012J\u000e\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u001e\u001a\u00020\u001fR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006!"}, d2 = {"Lcom/meshcommand/app/tactical/MissionReplayEngine;", "", "positionHistoryDao", "Lcom/meshcommand/app/data/dao/PositionHistoryDao;", "(Lcom/meshcommand/app/data/dao/PositionHistoryDao;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/meshcommand/app/tactical/MissionReplayEngine$ReplayState;", "allPositions", "", "Lcom/meshcommand/app/data/entity/PositionHistoryEntity;", "isRunning", "", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadMission", "", "startMs", "", "endMs", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pause", "play", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "reset", "seekTo", "timeMs", "setSpeed", "speed", "", "ReplayState", "app_debug"})
public final class MissionReplayEngine {
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.data.dao.PositionHistoryDao positionHistoryDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.meshcommand.app.tactical.MissionReplayEngine.ReplayState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.tactical.MissionReplayEngine.ReplayState> state = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.meshcommand.app.data.entity.PositionHistoryEntity> allPositions;
    @kotlin.jvm.Volatile()
    private volatile boolean isRunning = false;
    
    @javax.inject.Inject()
    public MissionReplayEngine(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.dao.PositionHistoryDao positionHistoryDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.meshcommand.app.tactical.MissionReplayEngine.ReplayState> getState() {
        return null;
    }
    
    /**
     * Load positions for a given time range.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object loadMission(long startMs, long endMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Start or resume playback.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object play(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void pause() {
    }
    
    public final void setSpeed(float speed) {
    }
    
    public final void seekTo(long timeMs) {
    }
    
    public final void reset() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BM\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\u0014\b\u0002\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000b\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\tH\u00c6\u0003J\u0015\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000bH\u00c6\u0003JQ\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\t2\u0014\b\u0002\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000bH\u00c6\u0001J\u0013\u0010\u001f\u001a\u00020\u00032\b\u0010 \u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010!\u001a\u00020\fH\u00d6\u0001J\t\u0010\"\u001a\u00020#H\u00d6\u0001R\u001d\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0014R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012\u00a8\u0006$"}, d2 = {"Lcom/meshcommand/app/tactical/MissionReplayEngine$ReplayState;", "", "isPlaying", "", "currentTimeMs", "", "startTimeMs", "endTimeMs", "speed", "", "currentPositions", "", "", "Lcom/meshcommand/app/data/entity/PositionHistoryEntity;", "(ZJJJFLjava/util/Map;)V", "getCurrentPositions", "()Ljava/util/Map;", "getCurrentTimeMs", "()J", "getEndTimeMs", "()Z", "getSpeed", "()F", "getStartTimeMs", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "toString", "", "app_debug"})
    public static final class ReplayState {
        private final boolean isPlaying = false;
        private final long currentTimeMs = 0L;
        private final long startTimeMs = 0L;
        private final long endTimeMs = 0L;
        private final float speed = 0.0F;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Map<java.lang.Integer, com.meshcommand.app.data.entity.PositionHistoryEntity> currentPositions = null;
        
        public ReplayState(boolean isPlaying, long currentTimeMs, long startTimeMs, long endTimeMs, float speed, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.Integer, com.meshcommand.app.data.entity.PositionHistoryEntity> currentPositions) {
            super();
        }
        
        public final boolean isPlaying() {
            return false;
        }
        
        public final long getCurrentTimeMs() {
            return 0L;
        }
        
        public final long getStartTimeMs() {
            return 0L;
        }
        
        public final long getEndTimeMs() {
            return 0L;
        }
        
        public final float getSpeed() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.Integer, com.meshcommand.app.data.entity.PositionHistoryEntity> getCurrentPositions() {
            return null;
        }
        
        public ReplayState() {
            super();
        }
        
        public final boolean component1() {
            return false;
        }
        
        public final long component2() {
            return 0L;
        }
        
        public final long component3() {
            return 0L;
        }
        
        public final long component4() {
            return 0L;
        }
        
        public final float component5() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.Integer, com.meshcommand.app.data.entity.PositionHistoryEntity> component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.meshcommand.app.tactical.MissionReplayEngine.ReplayState copy(boolean isPlaying, long currentTimeMs, long startTimeMs, long endTimeMs, float speed, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.Integer, com.meshcommand.app.data.entity.PositionHistoryEntity> currentPositions) {
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