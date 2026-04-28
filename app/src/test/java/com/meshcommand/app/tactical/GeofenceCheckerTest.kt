package com.meshcommand.app.tactical

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GeofenceCheckerTest {

    @Test
    fun `isPointInPolygon returns true when point is inside`() {
        val polygon = listOf(
            GeofenceChecker.LatLon(0.0, 0.0),
            GeofenceChecker.LatLon(0.0, 10.0),
            GeofenceChecker.LatLon(10.0, 10.0),
            GeofenceChecker.LatLon(10.0, 0.0)
        )
        val pointInside = GeofenceChecker.LatLon(5.0, 5.0)

        val result = GeofenceChecker.isPointInPolygon(pointInside, polygon)

        assertThat(result).isTrue()
    }

    @Test
    fun `isPointInPolygon returns false when point is outside`() {
        val polygon = listOf(
            GeofenceChecker.LatLon(0.0, 0.0),
            GeofenceChecker.LatLon(0.0, 10.0),
            GeofenceChecker.LatLon(10.0, 10.0),
            GeofenceChecker.LatLon(10.0, 0.0)
        )
        val pointOutside = GeofenceChecker.LatLon(15.0, 15.0)

        val result = GeofenceChecker.isPointInPolygon(pointOutside, polygon)

        assertThat(result).isFalse()
    }

    @Test
    fun `parsePolygonJson parses valid JSON correctly`() {
        val json = "[[21.0,105.0],[21.5,105.5]]"
        val result = GeofenceChecker.parsePolygonJson(json)

        assertThat(result).hasSize(2)
        assertThat(result[0].lat).isEqualTo(21.0)
        assertThat(result[0].lon).isEqualTo(105.0)
    }

    @Test
    fun `encodePolygonJson encodes polygon to JSON string`() {
        val points = listOf(
            GeofenceChecker.LatLon(21.0, 105.0),
            GeofenceChecker.LatLon(21.5, 105.5)
        )
        val json = GeofenceChecker.encodePolygonJson(points)

        assertThat(json).isEqualTo("[[21.0,105.0],[21.5,105.5]]")
    }

    @Test
    fun `checkViolations returns danger zone when point is inside restricted area`() {
        val fence = com.meshcommand.app.data.entity.GeofenceEntity(
            name = "Minefield",
            zoneType = com.meshcommand.app.data.entity.GeofenceEntity.TYPE_RESTRICTED,
            pointsJson = "[[0.0,0.0],[0.0,10.0],[10.0,10.0],[10.0,0.0]]",
            isActive = true
        )
        val violations = GeofenceChecker.checkViolations(5.0, 5.0, listOf(fence))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].name).isEqualTo("Minefield")
    }

    @Test
    fun `checkViolations returns safe zone when point leaves all safe areas`() {
        val safeZone = com.meshcommand.app.data.entity.GeofenceEntity(
            name = "Base Camp",
            zoneType = com.meshcommand.app.data.entity.GeofenceEntity.TYPE_SAFE,
            pointsJson = "[[0.0,0.0],[0.0,10.0],[10.0,10.0],[10.0,0.0]]",
            isActive = true
        )
        // Point is outside the polygon [0..10]
        val violations = GeofenceChecker.checkViolations(15.0, 15.0, listOf(safeZone))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].name).isEqualTo("Base Camp")
    }
}
