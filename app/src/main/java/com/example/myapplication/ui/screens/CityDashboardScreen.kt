package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.myapplication.core.domain.model.CityData
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

// ── Data models for display ────────────────────────────────────────────────
data class CategoryUi(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val icon: String
)

data class WeatherUi(
    val temperature: String = "--°C",
    val condition: String   = "Se încarcă...",
    val isLoading: Boolean  = true
)

/**
 * Main city dashboard screen.
 * 1:1 mapping of Dashboard.tsx from cluj-spirit---city-guide.
 *
 * Sections:
 *  • Points badge (top-right)
 *  • Hero animated blob image
 *  • 2-column grid: Weather + Interactive Map
 *  • Horizontal scroll "Descoperă Orașul" categories
 *  • FAB (orange, rounded-2xl)
 */
@Composable
fun CityDashboardScreen(
    cityName: String,
    cityImageUrl: String,
    points: Int,
    weather: WeatherUi = WeatherUi(),
    landmarks: List<CityData.AttractionData> = emptyList(),
    onLandmarkClick: (CityData.AttractionData) -> Unit = {},
    onMapClick: () -> Unit = {},
    onWeatherRefresh: () -> Unit = {},
    onFabClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Toate") }
    val filters = listOf("Toate", "Natură", "Istoric", "Muzee", "Artă")

    // Local filtering logic
    val filteredLandmarks = remember(searchQuery, selectedFilter, landmarks) {
        landmarks.filter {
            it.name.contains(searchQuery, ignoreCase = true) &&
            // Note: Currently no category property on AttractionData, so just filter by all if logic isn't mapped
            (selectedFilter == "Toate" || it.prompt?.contains(selectedFilter, ignoreCase = true) == true)
        }
    }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Scrollable Feed ────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                // 1. Points Badge & Hero Blob
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            PointsBadge(points = points)
                        }

                        // Hero Blob Image (animated morphing shape)
                        AnimatedBlobImage(imageUrl = cityImageUrl, cityName = cityName)
                    }
                }

                // 2. Weather + Map grid
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeatherCard(
                            weather   = weather,
                            cityName  = cityName,
                            onRefresh = onWeatherRefresh,
                            modifier  = Modifier.weight(1f)
                        )
                        MapCard(
                            cityName = cityName,
                            onClick  = onMapClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Search & Filter
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Explorează Atracții",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = OutfitFamily,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        GlassSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        FilterChipsRow(
                            filters = filters,
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )
                    }
                }

                // 4. Vertical Landmarks Feed
                items(filteredLandmarks) { attraction ->
                    // Make sure cards have side padding since LazyColumn doesn't have horizontal padding
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        LandmarkFeedCard(
                            title = attraction.name ?: "Atracție",
                            imageUrl = attraction.imageRes ?: "https://picsum.photos/seed/${attraction.name}/600/800",
                            onClick = { onLandmarkClick(attraction) }
                        )
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────────────────
            FloatingActionButton(
                onClick          = onFabClick,
                shape            = RoundedCornerShape(20.dp),
                containerColor   = ClujAccent,
                contentColor     = Color.Black,
                modifier         = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 24.dp)
                    .size(56.dp)
            ) {
                Icon(Icons.Filled.Apps, contentDescription = "Ghid Interactiv", modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ── Hero animated blob ─────────────────────────────────────────────────────
@Composable
private fun AnimatedBlobImage(imageUrl: String, cityName: String) {
    val transition = rememberInfiniteTransition(label = "blob")
    val radius1 by transition.animateFloat(
        initialValue  = 40f,
        targetValue   = 60f,
        animationSpec = infiniteRepeatable(
            animation  = tween(8000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "r1"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(AccentGlow, Color.Transparent)),
                    shape = RoundedCornerShape((radius1 / 2).dp)
                )
                .clip(RoundedCornerShape((radius1 / 2).dp))
        ) {
            AsyncImage(
                model              = imageUrl,
                contentDescription = cityName,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Soft accent glow overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors    = listOf(Color.Transparent, ClujDark.copy(0.5f)),
                            startY    = 120f,
                        )
                    )
            )
        }
    }
}

// ── Weather card ───────────────────────────────────────────────────────────
@Composable
private fun WeatherCard(
    weather: WeatherUi,
    cityName: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.height(180.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = "Vremea în $cityName",
                style      = MaterialTheme.typography.titleMedium,
                fontFamily = OutfitFamily,
                maxLines   = 2
            )

            Column {
                Text(
                    text       = weather.temperature,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    fontFamily = OutfitFamily
                )
                Text(
                    text       = weather.condition,
                    style      = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick  = onRefresh,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = AccentSubtle,
                    contentColor   = ClujAccent
                ),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizează", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Map card ───────────────────────────────────────────────────────────────
@Composable
private fun MapCard(
    cityName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hovered by remember { mutableStateOf(false) }
    val iconScale by animateFloatAsState(
        targetValue   = if (hovered) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "mapIconScale"
    )

    GlassCard(
        modifier = modifier
            .height(180.dp)
            .clickable {
                hovered = !hovered
                onClick()
            }
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        ClujAccent.copy(alpha = 0.10f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(iconScale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector   = Icons.Filled.Map,
                    contentDescription = "Hartă",
                    tint          = ClujAccent,
                    modifier      = Modifier.size(26.dp)
                )
            }

            Text(
                text       = "Harta Interactivă $cityName",
                style      = MaterialTheme.typography.titleMedium,
                fontFamily = OutfitFamily
            )
        }
    }
}

private val EaseInOutQuart = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
