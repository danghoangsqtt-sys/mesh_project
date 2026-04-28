package com.meshcommand.app.ui.theme

import androidx.compose.ui.graphics.Color

// Military Green Palette (matching desktop app)
object MeshColors {
    val BgDark = Color(0xFF3B5323)
    val BgLight = Color(0xFF4A5D23)
    val Accent = Color(0xFF556B2F)
    val Surface = Color(0xFF2C3E2D)
    val SurfaceDark = Color(0xFF1A2A1A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFE0E0E0)
    val TextMuted = Color(0xFFA0A0A0)

    // Status colors
    val StatusOk = Color(0xFF4CAF50)
    val StatusWarn = Color(0xFFFFC107)
    val StatusCrit = Color(0xFFF44336)
    val StatusOffline = Color(0xFF9E9E9E)

    // Button
    val ButtonPrimary = Color(0xFF6B8E23)
    val ButtonHover = Color(0xFF8F9779)
    val Border = Color(0xFF8F9779)

    // Info accent
    val InfoBlue = Color(0xFF2196F3)
}

fun alertColor(alertLevel: Int, isOnline: Boolean): Color {
    if (!isOnline) return MeshColors.StatusOffline
    return when {
        alertLevel >= 2 -> MeshColors.StatusCrit
        alertLevel == 1 -> MeshColors.StatusWarn
        else -> MeshColors.StatusOk
    }
}

fun alertText(alertLevel: Int, isOnline: Boolean): String {
    if (!isOnline) return "OFFLINE"
    return when {
        alertLevel >= 2 -> "CRITICAL"
        alertLevel == 1 -> "WARNING"
        else -> "OK"
    }
}
