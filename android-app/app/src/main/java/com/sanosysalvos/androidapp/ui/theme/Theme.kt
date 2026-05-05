package com.sanosysalvos.androidapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = Primary,
    primaryVariant = TextAccent,
    secondary = Secondary,
    background = Background,
    surface = Background,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSecondary = TextPrimary
)

private val DarkColorPalette = darkColors(
    primary = Primary,
)

@Composable
fun SanosYSalvosTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = androidx.compose.material.Typography(),
        shapes = androidx.compose.material.Shapes(),
        content = content
    )
}
