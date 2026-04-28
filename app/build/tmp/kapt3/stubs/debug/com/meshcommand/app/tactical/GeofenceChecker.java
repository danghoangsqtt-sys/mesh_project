package com.meshcommand.app.tactical;

/**
 * Geofence checker — uses ray-casting algorithm to determine
 * if a point is inside a polygon.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u000eB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006J\u001c\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006J\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\r\u001a\u00020\u0004\u00a8\u0006\u000f"}, d2 = {"Lcom/meshcommand/app/tactical/GeofenceChecker;", "", "()V", "encodePolygonJson", "", "points", "", "Lcom/meshcommand/app/tactical/GeofenceChecker$LatLon;", "isPointInPolygon", "", "point", "polygon", "parsePolygonJson", "json", "LatLon", "app_debug"})
public final class GeofenceChecker {
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.tactical.GeofenceChecker INSTANCE = null;
    
    private GeofenceChecker() {
        super();
    }
    
    /**
     * Ray-casting algorithm: checks if point P is inside polygon.
     * Returns true if the point is inside.
     */
    public final boolean isPointInPolygon(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.tactical.GeofenceChecker.LatLon point, @org.jetbrains.annotations.NotNull()
    java.util.List<com.meshcommand.app.tactical.GeofenceChecker.LatLon> polygon) {
        return false;
    }
    
    /**
     * Parse polygon points from JSON string.
     * Format: [[lat1,lon1],[lat2,lon2],...]
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.meshcommand.app.tactical.GeofenceChecker.LatLon> parsePolygonJson(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return null;
    }
    
    /**
     * Encode polygon points to JSON string.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String encodePolygonJson(@org.jetbrains.annotations.NotNull()
    java.util.List<com.meshcommand.app.tactical.GeofenceChecker.LatLon> points) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0013"}, d2 = {"Lcom/meshcommand/app/tactical/GeofenceChecker$LatLon;", "", "lat", "", "lon", "(DD)V", "getLat", "()D", "getLon", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class LatLon {
        private final double lat = 0.0;
        private final double lon = 0.0;
        
        public LatLon(double lat, double lon) {
            super();
        }
        
        public final double getLat() {
            return 0.0;
        }
        
        public final double getLon() {
            return 0.0;
        }
        
        public final double component1() {
            return 0.0;
        }
        
        public final double component2() {
            return 0.0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.meshcommand.app.tactical.GeofenceChecker.LatLon copy(double lat, double lon) {
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