package com.nate.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.nate.core.common.theme.ThemeTokens

@OptIn(ExperimentalTvMaterial3Api::class)
val TvDarkColorScheme = darkColorScheme(
    background = ThemeTokens.BackgroundBlack,
    primary = ThemeTokens.PrimaryBlue,
    secondary = ThemeTokens.AccentBurgundy,
    surface = ThemeTokens.SurfaceDarkBlue,
    onBackground = ThemeTokens.TextWhite,
    onPrimary = ThemeTokens.TextWhite,
    onSecondary = ThemeTokens.TextWhite,
    onSurface = ThemeTokens.TextWhite,
    border = ThemeTokens.FocusGold
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OnStreamTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        content = content
    )
}
