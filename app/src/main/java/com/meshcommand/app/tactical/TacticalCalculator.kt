package com.meshcommand.app.tactical

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Tactical calculator for distance, bearing, and navigation between GPS coordinates.
 * Uses Haversine formula for great-circle distance.
 */
object TacticalCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculate great-circle distance between two GPS points using Haversine formula.
     * @return distance in meters
     */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculate initial bearing from point 1 to point 2.
     * @return bearing in degrees (0-360, clockwise from north)
     */
    fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }

    /**
     * Format distance for display.
     */
    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "${meters.toInt()}m"
            meters < 10000 -> "${"%.1f".format(meters / 1000)}km"
            else -> "${(meters / 1000).toInt()}km"
        }
    }

    /**
     * Format bearing with compass direction.
     */
    fun formatBearing(degrees: Double): String {
        val direction = when {
            degrees < 22.5 || degrees >= 337.5 -> "N"
            degrees < 67.5 -> "NE"
            degrees < 112.5 -> "E"
            degrees < 157.5 -> "SE"
            degrees < 202.5 -> "S"
            degrees < 247.5 -> "SW"
            degrees < 292.5 -> "W"
            else -> "NW"
        }
        return "${degrees.toInt()}° $direction"
    }

    /**
     * Calculate Military Grid Reference System (MGRS) approximation from lat/lon.
     * Returns a simplified 6-digit grid reference.
     */
    fun toGridRef(lat: Double, lon: Double): String {
        val easting = ((lon + 180) * 10000).toInt() % 1000000
        val northing = ((lat + 90) * 10000).toInt() % 1000000
        return "${easting / 1000}${northing / 1000}"
    }
}
