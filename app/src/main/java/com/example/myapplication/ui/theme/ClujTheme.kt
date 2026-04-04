package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ── Dark Color Scheme matching Cluj Spirit ─────────────────────────────────
private val ClujDarkColorScheme = darkColorScheme(
    primary          = ClujAccent,
    onPrimary        = TextPrimary,
    primaryContainer = AccentSubtle,
    secondary        = ClujGold,
    onSecondary      = TextPrimary,
    surface          = GlassSurface,
    onSurface        = TextPrimary,
    onSurfaceVariant = TextSecondary,
    background       = ClujDark,
    onBackground     = TextPrimary,
    outline          = GlassBorder,
    error            = ClujAccent,
)

/**
 * Cluj Spirit theme wrapper.
 * Usage:
 *   ClujTheme { YourComposable() }
 */
@Composable
fun ClujTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClujDarkColorScheme,
        typography  = ClujTypography,
        shapes      = ClujShapes,
        content     = content
    )
}
