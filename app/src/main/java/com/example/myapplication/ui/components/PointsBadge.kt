package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

/**
 * The floating "★ Puncte: N" badge from the React Dashboard:
 *   bg-black/40 backdrop-blur-md border border-cluj-accent/30 rounded-full
 *
 * Matches the top-right pill with golden star + white text + orange glow.
 * Pulses gently to draw attention to new points.
 */
@Composable
fun PointsBadge(
    points: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pointsGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue  = 0.5f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation       = 12.dp,
                shape           = RoundedCornerShape(50),
                ambientColor    = ClujAccent.copy(alpha = glowAlpha),
                spotColor       = ClujAccent.copy(alpha = glowAlpha),
            )
            .clip(RoundedCornerShape(50))
            .background(Color(0x66000000))
            .border(1.dp, AccentGlow, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Gold star circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ClujGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "★",
                    fontSize = 10.sp,
                    color    = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Puncte: ",
                fontSize   = 13.sp,
                color      = Color.White,
                fontFamily = InterFamily
            )
            Text(
                text       = points.toString(),
                fontSize   = 13.sp,
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFamily
            )
        }
    }
}
