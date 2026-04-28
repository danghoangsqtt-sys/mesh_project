package com.meshcommand.app.ui.map;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000bJ\u0006\u0010\u001b\u001a\u00020\u0019J\u0015\u0010\u001c\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\u001dJ\u0016\u0010\u001e\u001a\u00020\u00192\u0006\u0010\u001f\u001a\u00020\b2\u0006\u0010 \u001a\u00020\bR\"\u0010\u0005\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\t\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R%\u0010\f\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b\u0018\u00010\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR%\u0010\u0010\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b\u0018\u00010\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0019\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000fR\u001d\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/meshcommand/app/ui/map/MapViewModel;", "Landroidx/lifecycle/ViewModel;", "soldierRepository", "Lcom/meshcommand/app/data/SoldierRepository;", "(Lcom/meshcommand/app/data/SoldierRepository;)V", "_cameraTarget", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lkotlin/Pair;", "", "_gatewayPosition", "_selectedSoldierId", "", "cameraTarget", "Lkotlinx/coroutines/flow/StateFlow;", "getCameraTarget", "()Lkotlinx/coroutines/flow/StateFlow;", "gatewayPosition", "getGatewayPosition", "selectedSoldierId", "getSelectedSoldierId", "soldierPositions", "", "Lcom/meshcommand/app/data/entity/SoldierEntity;", "getSoldierPositions", "centerOnSoldier", "", "nodeId", "clearCameraTarget", "selectSoldier", "(Ljava/lang/Integer;)V", "updateGatewayPosition", "lat", "lon", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class MapViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.meshcommand.app.data.SoldierRepository soldierRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> soldierPositions = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> _gatewayPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> gatewayPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _selectedSoldierId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> selectedSoldierId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> _cameraTarget = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> cameraTarget = null;
    
    @javax.inject.Inject()
    public MapViewModel(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.SoldierRepository soldierRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.meshcommand.app.data.entity.SoldierEntity>> getSoldierPositions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> getGatewayPosition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSelectedSoldierId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<kotlin.Pair<java.lang.Double, java.lang.Double>> getCameraTarget() {
        return null;
    }
    
    public final void selectSoldier(@org.jetbrains.annotations.Nullable()
    java.lang.Integer nodeId) {
    }
    
    public final void centerOnSoldier(int nodeId) {
    }
    
    public final void updateGatewayPosition(double lat, double lon) {
    }
    
    public final void clearCameraTarget() {
    }
}