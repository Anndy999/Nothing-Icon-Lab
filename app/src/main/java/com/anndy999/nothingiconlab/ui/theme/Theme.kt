package com.anndy999.nothingiconlab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NothingRed = Color(0xFFD71921)
val NothingBlack = Color(0xFF000000)
val NothingWhite = Color(0xFFFFFFFF)
val NothingGray = Color(0xFF1A1A1A)
val NothingMuted = Color(0xFF8A8A8A)

private val DarkColors = darkColorScheme(
    primary = NothingWhite,
    onPrimary = NothingBlack,
    secondary = NothingRed,
    onSecondary = NothingWhite,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingGray,
    onSurface = NothingWhite,
    surfaceVariant = Color(0xFF111111),
    onSurfaceVariant = NothingMuted,
    outline = Color(0xFF2A2A2A),
)

private val LightColors = lightColorScheme(
    primary = NothingBlack,
    onPrimary = NothingWhite,
    secondary = NothingRed,
    onSecondary = NothingWhite,
    background = NothingWhite,
    onBackground = NothingBlack,
    surface = Color(0xFFF4F4F4),
    onSurface = NothingBlack,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFDDDDDD),
)

@Composable
fun NothingIconLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
