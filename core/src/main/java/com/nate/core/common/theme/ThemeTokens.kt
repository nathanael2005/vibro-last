package com.nate.core.common.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared design tokens for the OnStream video streaming application.
 * Contains colors, typography scaling ratios, and standard layout padding grids.
 */
object ThemeTokens {

    // Brand Colors (Cinematic Dark Mode)
    val PrimaryBlue = Color(0xFF082351)      // Deep Cinematic Blue
    val AccentBurgundy = Color(0xFF5D0034)   // Burgundy Wine
    val BackgroundBlack = Color(0xFF060913)  // Clean Dark Cinematic background
    val SurfaceDarkBlue = Color(0xFF111625)  // Material card background
    val FocusGold = Color(0xFFFFC107)        // Amber Gold for TV Card Focus border

    // Text Colors
    val TextWhite = Color(0xFFFFFFFF)
    val TextGrey = Color(0xFFA0A5B5)
    val TextSecondary = Color(0xFFA0A5B5)
    val TextDisabled = Color(0xFF555964)

    // Layout Grids and Spacing
    val SpacingNone = 0.dp
    val SpacingXXS = 2.dp
    val SpacingXS = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 16.dp
    val SpacingXL = 24.dp
    val SpacingXXL = 32.dp
    val SpacingGiant = 48.dp

    // Safe Area Margins
    val MobileMargin = 16.dp
    val TvSafeMarginHorizontal = 56.dp
    val TvSafeMarginVertical = 36.dp

    // Typography Sizes
    val TitleGiant = 40.sp
    val TitleLarge = 24.sp
    val TitleMedium = 20.sp
    val TitleSmall = 16.sp
    val BodyMedium = 14.sp
    val BodySmall = 12.sp
    val LabelSmall = 10.sp
}
