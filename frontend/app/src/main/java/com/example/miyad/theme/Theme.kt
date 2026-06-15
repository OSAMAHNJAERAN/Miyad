package com.example.miyad.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MiyadLime,
    onPrimary = MiyadDarkBackground,
    primaryContainer = Color(0xFF2A3215),
    onPrimaryContainer = Color(0xFFE8F5B3),
    background = MiyadDarkBackground,
    onBackground = Color.White,
    surface = MiyadCardDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF20231B),
    onSurfaceVariant = Color(0xFFC6CCBF),
    outline = Color(0xFF3B4034),
    error = MiyadDangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MiyadLime,
    onPrimary = MiyadDarkBackground,
    primaryContainer = Color(0xFFEAF3C8),
    onPrimaryContainer = MiyadTextDark,
    background = MiyadPageBackground,
    onBackground = MiyadTextDark,
    surface = Color.White,
    onSurface = MiyadTextDark,
    surfaceVariant = Color(0xFFEDF3E8),
    onSurfaceVariant = Color(0xFF596359),
    outline = MiyadBorder,
    secondary = MiyadCardDark,
    onSecondary = Color.White,
    error = MiyadDangerRed,
    onError = Color.White
)

@Immutable
data class MiyadGlassColors(
    val surface: Color,
    val strongSurface: Color,
    val border: Color,
    val mutedText: Color,
    val glow: Color,
)

private val LightGlassColors = MiyadGlassColors(
    surface = Color.White.copy(alpha = 0.72f),
    strongSurface = Color.White.copy(alpha = 0.9f),
    border = Color.White.copy(alpha = 0.92f),
    mutedText = Color(0xFF667066),
    glow = MiyadLime.copy(alpha = 0.16f),
)

private val DarkGlassColors = MiyadGlassColors(
    surface = Color(0xFF171A13).copy(alpha = 0.9f),
    strongSurface = Color(0xFF20241A).copy(alpha = 0.98f),
    border = Color(0xFF3B4034).copy(alpha = 0.82f),
    mutedText = Color(0xFFC0C6B9),
    glow = MiyadLime.copy(alpha = 0.1f),
)

val LocalMiyadGlassColors = staticCompositionLocalOf { LightGlassColors }

@Composable
fun MiyadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassColors = if (darkTheme) DarkGlassColors else LightGlassColors

    androidx.compose.runtime.CompositionLocalProvider(
        LocalMiyadGlassColors provides glassColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
