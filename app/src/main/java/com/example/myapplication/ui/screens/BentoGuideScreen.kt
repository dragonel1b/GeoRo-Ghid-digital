package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AmbientGlowBackground
import com.example.myapplication.ui.components.GlassCard
import com.example.myapplication.ui.theme.*

data class BentoItem(
    val title: String,
    val imageUrl: String,
    val icon: String,
    val spanFull: Boolean = false // col-span-2 in React
)

/**
 * Bento Guide screen — Compose port of BentoGuide.tsx.
 *
 * A 2-column grid where some items are full-width (spanFull = true)
 * and others share a row. Each card has:
 *  • Background image (coverCrop)
 *  • Black/60 overlay
 *  • Centered icon badge + title
 *  • Bottom-right arrow button
 *  • whileHover scale animation on press
 */
@Composable
fun BentoGuideScreen(
    items: List<BentoItem> = defaultBentoItems(),
    onBack: () -> Unit = {},
    onItemClick: (BentoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Înapoi", tint = Color.White)
                }
                Text(
                    text       = "Bento Interactive Guide",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    fontFamily = OutfitFamily
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Bento Grid — we iterate and pair items
            val grouped = mutableListOf<List<BentoItem>>()
            var i = 0
            while (i < items.size) {
                if (items[i].spanFull) {
                    grouped.add(listOf(items[i]))
                    i++
                } else if (i + 1 < items.size && !items[i + 1].spanFull) {
                    grouped.add(listOf(items[i], items[i + 1]))
                    i += 2
                } else {
                    grouped.add(listOf(items[i]))
                    i++
                }
            }

            grouped.forEach { row ->
                if (row.size == 1) {
                    BentoCard(
                        item    = row[0],
                        onClick = { onItemClick(row[0]) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { item ->
                            BentoCard(
                                item     = item,
                                onClick  = { onItemClick(item) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoCard(
    item: BentoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "bentoScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable {
                pressed = !pressed
                onClick()
            }
    ) {
        // Background image
        AsyncImage(
            model              = item.imageUrl,
            contentDescription = item.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // Black overlay (bg-black/60)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
        )

        // Center content: icon + title
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ClujAccent.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 28.sp)
            }
            Text(
                text       = item.title,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                fontFamily = OutfitFamily
            )
        }

        // Bottom-right arrow button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .border(1.dp, GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = "Deschide",
                tint               = Color.White,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

fun defaultBentoItems() = listOf(
    BentoItem("Viața Universitară",    "https://picsum.photos/seed/bento-0/600/400", "🎓", spanFull = true),
    BentoItem("Atracții Turistice",    "https://picsum.photos/seed/bento-1/600/400", "📷"),
    BentoItem("Cultură și Festivaluri","https://picsum.photos/seed/bento-2/600/400", "🎵"),
    BentoItem("Natură și Parcuri",     "https://picsum.photos/seed/bento-3/600/400", "🌿", spanFull = true),
)
