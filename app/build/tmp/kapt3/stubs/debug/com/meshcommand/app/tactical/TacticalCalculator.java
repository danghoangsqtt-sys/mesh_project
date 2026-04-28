package com.meshcommand.app.tactical;

/**
 * Tactical calculator for distance, bearing, and navigation between GPS coordinates.
 * Uses Haversine formula for great-circle distance.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J&\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004J&\u0010\n\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0004J\u000e\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0004J\u0016\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/meshcommand/app/tactical/TacticalCalculator;", "", "()V", "EARTH_RADIUS_METERS", "", "bearingDegrees", "lat1", "lon1", "lat2", "lon2", "distanceMeters", "formatBearing", "", "degrees", "formatDistance", "meters", "toGridRef", "lat", "lon", "app_debug"})
public final class TacticalCalculator {
    private static final double EARTH_RADIUS_METERS = 6371000.0;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.tactical.TacticalCalculator INSTANCE = null;
    
    private TacticalCalculator() {
        super();
    }
    
    /**
     * Calculate great-circle distance between two GPS points using Haversine formula.
     * @return distance in meters
     */
    public final double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        return 0.0;
    }
    
    /**
     * Calculate initial bearing from point 1 to point 2.
     * @return bearing in degrees (0-360, clockwise from north)
     */
    public final double bearingDegrees(double lat1, double lon1, double lat2, double lon2) {
        return 0.0;
    }
    
    /**
     * Format distance for display.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatDistance(double meters) {
        return null;
    }
    
    /**
     * Format bearing with compass direction.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatBearing(double degrees) {
        return null;
    }
    
    /**
     * Calculate Military Grid Reference System (MGRS) approximation from lat/lon.
     * Returns a simplified 6-digit grid reference.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String toGridRef(double lat, double lon) {
        return null;
    }
}