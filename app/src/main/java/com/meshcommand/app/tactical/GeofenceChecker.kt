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
}
