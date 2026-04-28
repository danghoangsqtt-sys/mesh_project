package com.meshcommand.app.ui.map;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000^\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\u001a\u001a\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a\u0018\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0002\u001a\u0018\u0010\r\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0002\u001a\u0018\u0010\u000e\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0002\u001a\u0018\u0010\u000f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0002\u001a*\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\u0018\u0010\u0013\u001a\u0014\u0012\u0004\u0012\u00020\u000b\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\u0014H\u0002\u001a\u0010\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019H\u0002\u001a<\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u001b\u001a\u00020\u001c2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00152\u0014\u0010\u001f\u001a\u0010\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020!\u0018\u00010 H\u0002\u001a\u001e\u0010\"\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u0015H\u0002\u001a>\u0010#\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00152\u0014\u0010\u001f\u001a\u0010\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020!\u0018\u00010 2\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u0002\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"TRAIL_COLORS", "", "MapScreen", "", "viewModel", "Lcom/meshcommand/app/ui/map/MapViewModel;", "modifier", "Landroidx/compose/ui/Modifier;", "createCircleMarker", "Landroid/graphics/Bitmap;", "color", "", "size", "createDiamondMarker", "createStarMarker", "createTriangleMarker", "drawTrails", "map", "Lorg/maplibre/android/maps/MapLibreMap;", "trails", "", "", "Lcom/meshcommand/app/data/entity/PositionHistoryEntity;", "setupAdvancedLayers", "style", "Lorg/maplibre/android/maps/Style;", "setupMap", "context", "Landroid/content/Context;", "soldiers", "Lcom/meshcommand/app/data/entity/SoldierEntity;", "gatewayPos", "Lkotlin/Pair;", "", "updateGeoJsonSources", "updateMarkers", "app_debug"})
public final class MapScreenKt {
    @org.jetbrains.annotations.NotNull()
    private static final int[] TRAIL_COLORS = null;
    
    @androidx.compose.runtime.Composable()
    public static final void MapScreen(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.ui.map.MapViewModel viewModel, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    private static final void setupMap(org.maplibre.android.maps.MapLibreMap map, android.content.Context context, java.util.List<com.meshcommand.app.data.entity.SoldierEntity> soldiers, kotlin.Pair<java.lang.Double, java.lang.Double> gatewayPos) {
    }
    
    private static final void setupAdvancedLayers(org.maplibre.android.maps.Style style) {
    }
    
    private static final void updateGeoJsonSources(org.maplibre.android.maps.MapLibreMap map, java.util.List<com.meshcommand.app.data.entity.SoldierEntity> soldiers) {
    }
    
    private static final void updateMarkers(org.maplibre.android.maps.MapLibreMap map, java.util.List<com.meshcommand.app.data.entity.SoldierEntity> soldiers, kotlin.Pair<java.lang.Double, java.lang.Double> gatewayPos, com.meshcommand.app.ui.map.MapViewModel viewModel) {
    }
    
    private static final void drawTrails(org.maplibre.android.maps.MapLibreMap map, java.util.Map<java.lang.Integer, ? extends java.util.List<com.meshcommand.app.data.entity.PositionHistoryEntity>> trails) {
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