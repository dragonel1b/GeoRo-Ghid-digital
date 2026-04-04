package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.*

/**
 * Horizontal category card matching the "Descoperă Orașul" scroll section
 * from the React Dashboard:
 *   min-w-[280px] glass-card overflow-hidden group cursor-pointer
 *   with h-32 image + black/40 overlay + icon badge + title
 *   + description text below
 *
 * On hover (press) it scales up slightly — matching group-hover:scale-110.
 */
@Composable
fun CategoryCard(
    title: String,
    description: String,
    imageUrl: String,
    iconEmoji: String = "📷",
    accentColor: Color = ClujAccent,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cardScale"
    )

    GlassCard(
        modifier = modifier
            .width(280.dp)
            .scale(scale)
            .clickable {
                pressed = !pressed
                onClick()
            }
    ) {
        Column {
            // Image header (h-32 equivalent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            ) {
                AsyncImage(
                    model          = imageUrl,
                    contentDescription = title,
                    contentScale   = ContentScale.Crop,
                    modifier       = Modifier.fillMaxSize()
                )

                // Dark overlay (bg-black/40)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                )

                // Icon badge + title (top-left)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalAlignment       = Alignment.CenterVertically,
                    horizontalArrangement   = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = iconEmoji, fontSize = 18.sp)
                    }
                    Text(
                        text       = title,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        fontFamily = OutfitFamily
                    )
                }
            }

            // Description body (p-4)
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text      = description,
                    color     = TextSecondary,
                    fontSize  = 12.sp,
                    maxLines  = 2,
                    overflow  = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    fontFamily = InterFamily
                )
            }
        }
    }
}
