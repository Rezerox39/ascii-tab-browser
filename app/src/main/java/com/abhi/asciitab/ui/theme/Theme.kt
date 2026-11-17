package com.abhi.asciitab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AmoledBlack = Color(0xFF000000)
val GlassSurface = Color(0x8C0C1018)
val GlassElevated = Color(0xB8121826)
val Hairline = Color(0x1FFFFFFF)
val AccentBlue = Color(0xFF4DA3FF)
val DimText = Color(0xFF8A94A6)
val Danger = Color(0xFFFF5C5C)

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    background = AmoledBlack,
    onBackground = Color.White,
    surface = GlassSurface,
    onSurface = Color.White,
    error = Danger
)

@Composable
fun AsciiTabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AsciiTabTypography,
        content = content
    )
}
