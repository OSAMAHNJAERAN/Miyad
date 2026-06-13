package com.example.miyad.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MiyadLime,
    onPrimary = MiyadDarkBackground,
    background = MiyadDarkBackground,
    onBackground = Color.White,
    surface = MiyadCardDark,
    onSurface = Color.White,
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
    secondary = MiyadCardDark,
    onSecondary = Color.White,
    error = MiyadDangerRed,
    onError = Color.White
)

@Composable
fun MiyadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce our custom design system
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
