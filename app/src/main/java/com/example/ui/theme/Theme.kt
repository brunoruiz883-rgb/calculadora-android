package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NeumorphicColors(
    val background: Color,
    val surface: Color,
    val surfacePressed: Color,
    val lightShadow: Color,
    val darkShadow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val equalsColor: Color,
    val isDark: Boolean
)

val DarkNeumorphicColors = NeumorphicColors(
    background = NeoDarkBackground,
    surface = NeoDarkSurface,
    surfacePressed = NeoDarkSurfacePressed,
    lightShadow = NeoDarkLightShadow,
    darkShadow = NeoDarkDarkShadow,
    textPrimary = NeoDarkTextPrimary,
    textSecondary = NeoDarkTextSecondary,
    accent = NeoDarkAccent,
    accentSecondary = NeoDarkAccentSecondary,
    equalsColor = NeoDarkEquals,
    isDark = true
)

val LightNeumorphicColors = NeumorphicColors(
    background = NeoLightBackground,
    surface = NeoLightSurface,
    surfacePressed = NeoLightSurfacePressed,
    lightShadow = NeoLightLightShadow,
    darkShadow = NeoLightDarkShadow,
    textPrimary = NeoLightTextPrimary,
    textSecondary = NeoLightTextSecondary,
    accent = NeoLightAccent,
    accentSecondary = NeoLightAccentSecondary,
    equalsColor = NeoLightEquals,
    isDark = false
)

val LocalNeumorphicColors = staticCompositionLocalOf { DarkNeumorphicColors }

private val M3DarkColorScheme = darkColorScheme(
    primary = NeoDarkAccent,
    secondary = NeoDarkAccentSecondary,
    background = NeoDarkBackground,
    surface = NeoDarkSurface,
    onBackground = NeoDarkTextPrimary,
    onSurface = NeoDarkTextPrimary
)

private val M3LightColorScheme = lightColorScheme(
    primary = NeoLightAccent,
    secondary = NeoLightAccentSecondary,
    background = NeoLightBackground,
    surface = NeoLightSurface,
    onBackground = NeoLightTextPrimary,
    onSurface = NeoLightTextPrimary
)

@Composable
fun NeumorphicAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val neoColors = if (darkTheme) DarkNeumorphicColors else LightNeumorphicColors
    val m3Scheme = if (darkTheme) M3DarkColorScheme else M3LightColorScheme

    CompositionLocalProvider(LocalNeumorphicColors provides neoColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = Typography,
            content = content
        )
    }
}

// Backward-compatible alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NeumorphicAppTheme(darkTheme = darkTheme, content = content)
}
