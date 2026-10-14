package com.togglestudios.lumenpop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GlowCyan,
    secondary = GlowGold,
    tertiary = GlowPink,
    background = CosmicDark,
    surface = CosmicSurface,
    onPrimary = CosmicDark,
    onSecondary = CosmicDark,
    onTertiary = CosmicDark,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun LumenPopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
