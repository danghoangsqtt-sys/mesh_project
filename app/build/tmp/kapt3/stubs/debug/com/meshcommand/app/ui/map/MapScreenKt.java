package com.meshcommand.app.ui.map;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0018\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0002\u001a\u0018\u0010\u000b\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0002\u001a\u0018\u0010\f\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0002\u001a\u0018\u0010\r\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0002\u001a<\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\u0014\u0010\u0016\u001a\u0010\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u0018\u0018\u00010\u0017H\u0002\u001a>\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\u0014\u0010\u0016\u001a\u0010\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u0018\u0018\u00010\u00172\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003H\u0002\u00a8\u0006\u001a"}, d2 = {"MapScreen", "", "viewModel", "Lcom/meshcommand/app/ui/map/MapViewModel;", "modifier", "Landroidx/compose/ui/Modifier;", "createCircleMarker", "Landroid/graphics/Bitmap;", "color", "", "size", "createDiamondMarker", "createStarMarker", "createTriangleMarker", "setupMap", "map", "Lorg/maplibre/android/maps/MapLibreMap;", "context", "Landroid/content/Context;", "soldiers", "", "Lcom/meshcommand/app/data/entity/SoldierEntity;", "gatewayPos", "Lkotlin/Pair;", "", "updateMarkers", "app_debug"})
public final class MapScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void MapScreen(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.ui.map.MapViewModel viewModel, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    private static final void setupMap(org.maplibre.android.maps.MapLibreMap map, android.content.Context context, java.util.List<com.meshcommand.app.data.entity.SoldierEntity> soldiers, kotlin.Pair<java.lang.Double, java.lang.Double> gatewayPos) {
    }
    
    private static final void updateMarkers(org.maplibre.android.maps.MapLibreMap map, java.util.List<com.meshcommand.app.data.entity.SoldierEntity> soldiers, kotlin.Pair<java.lang.Double, java.lang.Double> gatewayPos, com.meshcommand.app.ui.map.MapViewModel viewModel) {
    }
    
    private static final android.graphics.Bitmap createCircleMarker(int color, int size) {
        return null;
    }
    
    private static final android.graphics.Bitmap createTriangleMarker(int color, int size) {
        return null;
    }
    
    private static final android.graphics.Bitmap createDiamondMarker(int color, int size) {
        return null;
    }
    
    private static final android.graphics.Bitmap createStarMarker(int color, int size) {
        return null;
    }
}