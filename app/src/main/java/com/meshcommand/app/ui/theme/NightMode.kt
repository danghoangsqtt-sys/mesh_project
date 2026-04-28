package com.meshcommand.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Night mode theme — red-on-black for preserving night vision.
 * Used during night operations to minimize eye strain.
 */
object NightModeColors {
    val BgDark = Color(0xFF0A0000)
    val BgLight = Color(0xFF1A0505)
    val Accent = Color(0xFF3D0000)
    val Surface = Color(0xFF110000)
    val SurfaceDark = Color(0xFF050000)
    val TextPrimary = Color(0xFFCC3333)
    val TextSecondary = Color(0xFF993333)
    val TextMuted = Color(0xFF663333)

    // Status — dimmer versions
    val StatusOk = Color(0xFF338833)
    val StatusWarn = Color(0xFFCC8800)
    val StatusCrit = Color(0xFFCC3333)
    val StatusOffline = Color(0xFF555555)

    // Button
    val ButtonPrimary = Color(0xFF661111)
    val Border = Color(0xFF441111)

    val InfoBlue = Color(0xFF334488)
}
