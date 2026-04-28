package com.meshcommand.app.tactical

/**
 * Geofence checker — uses ray-casting algorithm to determine
 * if a point is inside a polygon.
 */
object GeofenceChecker {

    data class LatLon(val lat: Double, val lon: Double)

    /**
     * Ray-casting algorithm: checks if point P is inside polygon.
     * Returns true if the point is inside.
     */
    fun isPointInPolygon(point: LatLon, polygon: List<LatLon>): Boolean {
        if (polygon.size < 3) return false

        var inside = false
        var j = polygon.size - 1

        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]

            if ((pi.lon > point.lon) != (pj.lon > point.lon) &&
                point.lat < (pj.lat - pi.lat) * (point.lon - pi.lon) / (pj.lon - pi.lon) + pi.lat
            ) {
                inside = !inside
            }
            j = i
        }

        return inside
    }

    /**
     * Parse polygon points from JSON string.
     * Format: [[lat1,lon1],[lat2,lon2],...]
     */
    fun parsePolygonJson(json: String): List<LatLon> {
        return try {
            val cleaned = json.trim().removePrefix("[").removeSuffix("]")
            val pairs = cleaned.split("],[", "], [")
            pairs.map { pair ->
                val coords = pair.removePrefix("[").removeSuffix("]").split(",")
                LatLon(coords[0].trim().toDouble(), coords[1].trim().toDouble())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Encode polygon points to JSON string.
     */
    fun encodePolygonJson(points: List<LatLon>): String {
        return points.joinToString(",", "[", "]") { "[${it.lat},${it.lon}]" }
    }

    /**
     * Checks if a given coordinate violates any active geofences.
     * Rule: Inside RESTRICTED = Violation. Outside all SAFE (if any exist) = Violation.
     */
    fun checkViolations(
        lat: Double,
        lon: Double,
        activeGeofences: List<com.meshcommand.app.data.entity.GeofenceEntity>
    ): List<com.meshcommand.app.data.entity.GeofenceEntity> {
        val point = LatLon(lat, lon)
        val violations = mutableListOf<com.meshcommand.app.data.entity.GeofenceEntity>()
        var inAnySafeZone = false
        var hasSafeZones = false

        for (fence in activeGeofences) {
            val polygon = parsePolygonJson(fence.pointsJson)
            val inside = isPointInPolygon(point, polygon)

            if (fence.zoneType == com.meshcommand.app.data.entity.GeofenceEntity.TYPE_RESTRICTED) {
                if (inside) {
                    violations.add(fence)
                }
            } else if (fence.zoneType == com.meshcommand.app.data.entity.GeofenceEntity.TYPE_SAFE) {
                hasSafeZones = true
                if (inside) {
                    inAnySafeZone = true
                }
            }
        }

        if (hasSafeZones && !inAnySafeZone) {
            val firstSafe = activeGeofences.firstOrNull { it.zoneType == com.meshcommand.app.data.entity.GeofenceEntity.TYPE_SAFE }
            if (firstSafe != null) {
                // If they are outside all safe zones, add one as the violation reason
                violations.add(firstSafe)
            }
        }

        return violations
    }
}
