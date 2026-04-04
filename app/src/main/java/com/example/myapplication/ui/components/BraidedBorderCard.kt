package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.ClujAccent
import com.example.myapplication.ui.theme.ClujGold
import com.example.myapplication.ui.theme.GlassSurface

/**
 * Card with a premium "braided" gradient border effect.
 * Matches the .braided-border CSS class + bg_braided_border.xml drawable.
 *
 * Implemented with Canvas drawBehind — draws a gradient stroke around
 * a glass inner surface, simulating the orange→gold gradient border.
 */
@Composable
fun BraidedBorderCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEC4899), // pink
            ClujAccent,        // orange
            ClujGold,          // gold
            ClujAccent,        // orange (loop)
            Color(0xFFEC4899)  // pink (loop)
        )
    )

    Box(
        modifier = modifier
            // 1. Draw the gradient border on the canvas behind the content
            .drawBehind {
                val strokeW = borderWidth.toPx()
                drawRoundRect(
                    brush = gradient,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeW)
                )
            }
            // 2. Clip inner content to slightly smaller rounded rect
            .padding(borderWidth)
            .clip(RoundedCornerShape(cornerRadius - 2.dp))
            .background(GlassSurface),
        content = content
    )
}
