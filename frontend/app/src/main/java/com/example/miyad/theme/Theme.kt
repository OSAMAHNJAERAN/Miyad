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
    background = MiyadDarkBackground,
    onBackground = Color.White,
    surface = MiyadCardDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF172418),
    onSurfaceVariant = Color(0xFFBAC7B8),
    outline = Color(0xFF334535),
    error = MiyadDangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MiyadLime,
    onPrimary = MiyadDarkBackground,
    background = MiyadPageBackground,
    onBackground = MiyadTextDark,
    surface = Color.White,
    onSurface = MiyadTextDark,
    surfaceVariant = Color(0xFFEAF0E6),
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
    glow = MiyadLime.copy(alpha = 0.18f),
)

private val DarkGlassColors = MiyadGlassColors(
    surface = Color(0xFF132014).copy(alpha = 0.78f),
    strongSurface = Color(0xFF172719).copy(alpha = 0.94f),
    border = Color(0xFF38503B).copy(alpha = 0.78f),
    mutedText = Color(0xFFB4C0B2),
    glow = MiyadLime.copy(alpha = 0.12f),
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
