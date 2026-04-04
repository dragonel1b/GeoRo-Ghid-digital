package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*

/**
 * Full-screen ambient background with animated glow blobs.
 * Matches the CSS background-image radial-gradient on body + the
 * individual glow blob Views from activity_enhanced_city.xml.
 *
 *  • Green blob: top-start (Cluj green tint)
 *  • Blue blob: center-end (Cluj blue tint)
 *  • Orange blob: bottom-start (accent tint)
 *
 * Blobs gently pulse using animateFloat for a subtle living effect.
 */
@Composable
fun AmbientGlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    // Animate blob opacity for a gentle pulse
    val greenAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.25f,
        animationSpec = infiniteRepeatable(
            animation  = tween(4000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "greenPulse"
    )
    val blueAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue  = 0.22f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5500, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bluePulse"
    )
    val orangeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue  = 0.18f,
        animationSpec = infiniteRepeatable(
            animation  = tween(7000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orangePulse"
    )

    Box(
        modifier = modifier
            .background(ClujDark)
            // Base gradient (matches CSS body background-image)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ClujGreen.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = 900f
                )
            )
    ) {
        // Green blob — top-start
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = (-30).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ClujGreen.copy(alpha = greenAlpha),
                            Color.Transparent
                        )
                    )
                )
                .blur(60.dp)
        )

        // Blue blob — center-end
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(androidx.compose.ui.Alignment.CenterEnd)
                .offset(x = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ClujBlue.copy(alpha = blueAlpha),
                            Color.Transparent
                        )
                    )
                )
                .blur(60.dp)
        )

        // Orange blob — bottom-start
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(androidx.compose.ui.Alignment.BottomStart)
                .offset(x = (-30).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ClujAccent.copy(alpha = orangeAlpha),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )

        // Actual screen content on top
        content()
    }
}

// Easing curves
private val EaseInOutQuart = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
