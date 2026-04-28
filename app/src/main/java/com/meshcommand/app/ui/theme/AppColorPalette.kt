package com.meshcommand.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Provides the active color palette based on day/night mode.
 * Components should use ActiveColors.current instead of directly referencing MeshColors.
 */
data class AppColorPalette(
    val bgDark: Color,
    val bgLight: Color,
    val accent: Color,
    val surface: Color,
    val surfaceDark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val statusOk: Color,
    val statusWarn: Color,
    val statusCrit: Color,
    val statusOffline: Color,
    val buttonPrimary: Color,
    val border: Color,
    val infoBlue: Color
)

val DayPalette = AppColorPalette(
    bgDark = MeshColors.BgDark,
    bgLight = MeshColors.BgLight,
    accent = MeshColors.Accent,
    surface = MeshColors.Surface,
    surfaceDark = MeshColors.SurfaceDark,
    textPrimary = MeshColors.TextPrimary,
    textSecondary = MeshColors.TextSecondary,
    textMuted = MeshColors.TextMuted,
    statusOk = MeshColors.StatusOk,
    statusWarn = MeshColors.StatusWarn,
    statusCrit = MeshColors.StatusCrit,
    statusOffline = MeshColors.StatusOffline,
    buttonPrimary = MeshColors.ButtonPrimary,
    border = MeshColors.Border,
    infoBlue = MeshColors.InfoBlue
)

val NightPalette = AppColorPalette(
    bgDark = NightModeColors.BgDark,
    bgLight = NightModeColors.BgLight,
    accent = NightModeColors.Accent,
    surface = NightModeColors.Surface,
    surfaceDark = NightModeColors.SurfaceDark,
    textPrimary = NightModeColors.TextPrimary,
    textSecondary = NightModeColors.TextSecondary,
    textMuted = NightModeColors.TextMuted,
    statusOk = NightModeColors.StatusOk,
    statusWarn = NightModeColors.StatusWarn,
    statusCrit = NightModeColors.StatusCrit,
    statusOffline = NightModeColors.StatusOffline,
    buttonPrimary = NightModeColors.ButtonPrimary,
    border = NightModeColors.Border,
    infoBlue = NightModeColors.InfoBlue
)

val LocalAppColors = compositionLocalOf { DayPalette }
