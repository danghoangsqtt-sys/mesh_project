package com.meshcommand.app.tactical

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TacticalCalculatorTest {

    @Test
    fun `distanceMeters calculates correct distance`() {
        val lat1 = 21.0285 // Hanoi
        val lon1 = 105.8542
        val lat2 = 10.8231 // HCM
        val lon2 = 106.6297
        
        val distance = TacticalCalculator.distanceMeters(lat1, lon1, lat2, lon2)
        
        // Hanoi to HCM is approx 1139 km
        assertThat(distance).isWithin(5000.0).of(1139000.0)
    }

    @Test
    fun `bearingDegrees calculates correct bearing`() {
        val lat1 = 21.0285
        val lon1 = 105.8542
        val lat2 = 21.0285
        val lon2 = 106.8542 // Directly East
        
        val bearing = TacticalCalculator.bearingDegrees(lat1, lon1, lat2, lon2)
        
        // East is 90 degrees
        assertThat(bearing).isWithin(1.0).of(90.0)
    }

    @Test
    fun `formatDistance formats meters correctly`() {
        assertThat(TacticalCalculator.formatDistance(500.0)).isEqualTo("500m")
        assertThat(TacticalCalculator.formatDistance(1500.0)).isEqualTo("1.5km")
        assertThat(TacticalCalculator.formatDistance(12000.0)).isEqualTo("12km")
    }

    @Test
    fun `formatBearing formats degrees correctly`() {
        assertThat(TacticalCalculator.formatBearing(0.0)).isEqualTo("0° N")
        assertThat(TacticalCalculator.formatBearing(45.0)).isEqualTo("45° NE")
        assertThat(TacticalCalculator.formatBearing(180.0)).isEqualTo("180° S")
    }
}
