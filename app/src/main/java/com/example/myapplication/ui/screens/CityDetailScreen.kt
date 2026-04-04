package com.example.myapplication.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.domain.model.CityData
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

/**
 * City Detail screen — Compose replacement for CityDetailActivity.java.
 *
 * Accesses [CityData] via Java getters (getId(), getName(), etc.)
 * and renders:
 *   • Full-width hero image with name overlay
 *   • Floating PointsBadge + back button
 *   • Description in BraidedBorderCard
 *   • Sections list with BraidedBorderCard
 *   • Weather, Map, Events, Tips cards (GlassCard)
 *   • Photo Challenge CTA (BraidedBorderCard)
 */
@Composable
fun CityDetailScreen(
    cityData: CityData,
    points: Int,
    weather: WeatherUi = WeatherUi(),
    onBack: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onWeatherRefresh: () -> Unit = {},
    onPhotoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Cache getters as local vals for readability
    val cityName       = cityData.name    ?: "Oraș"
    val cityId         = cityData.id      ?: "city"
    val cityDesc       = cityData.description
    val cityRegion     = cityData.region
    val heroUrl        = cityData.defaultImages?.firstOrNull()
                         ?: "https://picsum.photos/seed/$cityId/800/600"
    val sections       = cityData.sections   ?: emptyList()
    val events         = cityData.events     ?: emptyList()
    val tips           = cityData.tips       ?: emptyList()
    val mapCoords      = cityData.mapCoords

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // ── Hero image ────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        AsyncImage(
                            model              = heroUrl,
                            contentDescription = cityName,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                        // Gradient fade into bg
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, ClujDark)
                                    )
                                )
                        )
                        // City name
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Text(
                                text       = cityName,
                                fontSize   = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White,
                                fontFamily = OutfitFamily
                            )
                            if (!cityRegion.isNullOrBlank()) {
                                Text(
                                    text       = cityRegion,
                                    fontSize   = 14.sp,
                                    color      = ClujAccent,
                                    fontFamily = InterFamily
                                )
                            }
                        }
                    }
                }

                // ── Description ───────────────────────────────────────────
                if (!cityDesc.isNullOrBlank()) {
                    item {
                        BraidedBorderCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text       = "Despre Oraș",
                                    style      = MaterialTheme.typography.headlineMedium,
                                    fontFamily = OutfitFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text       = cityDesc,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                    fontFamily = InterFamily
                                )
                            }
                        }
                    }
                }

                // ── Sections ──────────────────────────────────────────────
                items(sections) { section ->
                    BraidedBorderCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text       = section.title ?: "",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontFamily = OutfitFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text       = section.content ?: "",
                                style      = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                fontFamily = InterFamily
                            )
                        }
                    }
                }

                // ── Weather widget ────────────────────────────────────────
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text       = "☁️ Vremea în $cityName",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontFamily = OutfitFamily
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (weather.isLoading) {
                                LinearProgressIndicator(
                                    color      = ClujAccent,
                                    trackColor = AccentSubtle,
                                    modifier   = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text       = "${weather.temperature}  ·  ${weather.condition}",
                                    color      = Color.White,
                                    fontSize   = 16.sp,
                                    fontFamily = InterFamily
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onWeatherRefresh,
                                colors  = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ClujAccent
                                ),
                                border  = BorderStroke(1.dp, AccentGlow),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Text("Actualizează", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ── Map shortcut ──────────────────────────────────────────
                if (!mapCoords.isNullOrBlank()) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { onMapClick() }
                        ) {
                            Row(
                                modifier              = Modifier.padding(20.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AccentSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🗺️", fontSize = 22.sp)
                                }
                                Column {
                                    Text(
                                        text       = "Harta Interactivă",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontFamily = OutfitFamily
                                    )
                                    Text(
                                        text  = "Deschide Google Maps →",
                                        color = ClujAccent,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Events ────────────────────────────────────────────────
                if (events.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text     = "🎉 Evenimente Locale",
                                style    = MaterialTheme.typography.headlineMedium,
                                fontFamily = OutfitFamily,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            events.forEach { event ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text     = "• $event",
                                        color    = TextSecondary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(16.dp),
                                        fontFamily = InterFamily
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tips ──────────────────────────────────────────────────
                if (tips.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text       = "💡 Sfaturi Locale",
                                    style      = MaterialTheme.typography.headlineMedium,
                                    fontFamily = OutfitFamily
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                tips.forEach { tip ->
                                    Text(
                                        text       = "• $tip",
                                        color      = TextSecondary,
                                        fontSize   = 14.sp,
                                        lineHeight = 22.sp,
                                        modifier   = Modifier.padding(bottom = 6.dp),
                                        fontFamily = InterFamily
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Photo challenge ───────────────────────────────────────
                item {
                    BraidedBorderCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text       = "📸 Provocare Foto",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontFamily = OutfitFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text       = "Imortalizează cele mai frumoase locuri din $cityName și adaugă-le în colecția ta!",
                                color      = TextSecondary,
                                fontSize   = 14.sp,
                                lineHeight = 22.sp,
                                fontFamily = InterFamily
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick      = onPhotoClick,
                                colors       = ButtonDefaults.buttonColors(
                                    containerColor = ClujAccent,
                                    contentColor   = Color.White
                                ),
                                shape        = RoundedCornerShape(20.dp),
                                modifier     = Modifier.fillMaxWidth()
                            ) {
                                Text("Adaugă Fotografie", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Floating top bar: Back + Points ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x66000000))
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.ArrowBack,
                        contentDescription = "Înapoi",
                        tint               = Color.White
                    )
                }
                PointsBadge(points = points)
            }
        }
    }
}
