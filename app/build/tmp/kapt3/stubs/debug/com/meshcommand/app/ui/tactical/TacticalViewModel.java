package com.meshcommand.app.ui.tactical;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u0007J1\u0010\u001c\u001a\u00020\u001a2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u001e2\b\b\u0002\u0010 \u001a\u00020\t2\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\"J\u000e\u0010#\u001a\u00020\u001a2\u0006\u0010!\u001a\u00020\tJ\u0006\u0010$\u001a\u00020\u001aR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\rR\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00110\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\r\u00a8\u0006%"}, d2 = {"Lcom/meshcommand/app/ui/tactical/TacticalViewModel;", "Landroidx/lifecycle/ViewModel;", "soldierRepository", "Lcom/meshcommand/app/data/SoldierRepository;", "(Lcom/meshcommand/app/data/SoldierRepository;)V", "_lastRxTime", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_selectedNodeId", "", "lastRxTime", "Lkotlinx/coroutines/flow/StateFlow;", "getLastRxTime", "()Lkotlinx/coroutines/flow/StateFlow;", "onlineCount", "getOnlineCount", "recentEvents", "", "Lcom/meshcommand/app/data/entity/EventEntity;", "getRecentEvents", "selectedNodeId", "getSelectedNodeId", "soldiers", "Lcom/meshcommand/app/data/entity/SoldierEntity;", "getSoldiers", "acknowledgeAlert", "", "eventId", "logEvent", "eventType", "", "message", "severity", "nodeId", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Integer;)V", "selectSoldier", "updateLastRxTime", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class TacticalViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.data.SoldierRepository soldierRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> soldiers = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> onlineCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> recentEvents = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _selectedNodeId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> selectedNodeId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _lastRxTime = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> lastRxTime = null;
    
    @javax.inject.Inject()
    public TacticalViewModel(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.SoldierRepository soldierRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getSoldiers() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getOnlineCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.data.entity.EventEntity>> getRecentEvents() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSelectedNodeId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getLastRxTime() {
        return null;
    }
    
    public final void selectSoldier(int nodeId) {
    }
    
    public final void updateLastRxTime() {
    }
    
    public final void acknowledgeAlert(long eventId) {
    }
    
    public final void logEvent(@org.jetbrains.annotations.NotNull()
    java.lang.String eventType, @org.jetbrains.annotations.NotNull()
    java.lang.String message, int severity, @org.jetbrains.annotations.Nullable()
    java.lang.Integer nodeId) {
    }
}