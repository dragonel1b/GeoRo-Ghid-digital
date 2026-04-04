package com.example.myapplication.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import com.example.myapplication.ui.theme.GlassBorder
import com.example.myapplication.ui.theme.GlassSurface

/**
 * Reusable glassmorphism card matching `.glass-card` from index.css:
 *   bg-white/5 backdrop-blur-md border border-white/10 rounded-3xl
 *
 * On Android we simulate the blur with a semi-transparent dark overlay
 * + white/5 background. True blur can be layered on top via Modifier.blur()
 * (API 31+) or BlurView composable if needed.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large.copy(
        topStart = androidx.compose.foundation.shape.CornerSize(cornerRadius),
        topEnd = androidx.compose.foundation.shape.CornerSize(cornerRadius),
        bottomStart = androidx.compose.foundation.shape.CornerSize(cornerRadius),
        bottomEnd = androidx.compose.foundation.shape.CornerSize(cornerRadius),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape),
        content = content
    )
}
