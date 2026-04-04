package com.example.myapplication.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.myapplication.core.domain.model.CityData
import com.example.myapplication.core.domain.repository.CityRepository
import com.example.myapplication.ui.screens.*

// ── Route constants ──────────────────────────────────────────────────────────
object CityNavRoutes {
    const val DASHBOARD   = "dashboard/{cityId}"
    const val CITY_DETAIL = "city_detail/{cityId}"
    const val BENTO_GUIDE = "bento_guide/{cityId}"

    fun dashboard(cityId: String)   = "dashboard/$cityId"
    fun cityDetail(cityId: String)  = "city_detail/$cityId"
    fun bentoGuide(cityId: String)  = "bento_guide/$cityId"
}

/**
 * Main navigation graph for the Compose screens.
 * Entry point: [CityNavRoutes.dashboard].
 *
 * The [startCityId] is passed from [ComposeEntryActivity]
 * (originally received from Intent.EXTRA_CITY_ID).
 */
@Composable
fun CityNavGraph(
    startCityId: String,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    NavHost(
        navController  = navController,
        startDestination = CityNavRoutes.dashboard(startCityId)
    ) {
        // ── Dashboard ──────────────────────────────────────────────────────
        composable(
            route     = CityNavRoutes.DASHBOARD,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cityId   = backStackEntry.arguments?.getString("cityId") ?: return@composable
            val cityData = remember(cityId) { CityRepository.getInstance().getCityById(context, cityId) }

            if (cityData != null) {
                CityDashboardScreen(
                    cityName    = cityData.name ?: "Oraș",
                    cityImageUrl= cityData.defaultImages?.firstOrNull()
                        ?: "https://picsum.photos/seed/${cityData.id}/800/600",
                    points      = 0, // TODO: wire up PointsManager
                    landmarks   = cityData.attractions ?: emptyList(),
                    onLandmarkClick = { attraction ->
                        // Navigate to city detail or specific attraction
                        navController.navigate(CityNavRoutes.cityDetail(cityId)) 
                    },
                    onMapClick  = {
                        val coords = cityData.mapCoords ?: ""
                        val cName  = cityData.name ?: "Romania"
                        val uri    = android.net.Uri.parse("geo:$coords?q=$cName,Romania")
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        try { context.startActivity(intent) } catch (e: Exception) {
                            intent.setPackage(null)
                            try { context.startActivity(intent) } catch (ignored: Exception) {}
                        }
                    },
                    onFabClick  = { navController.navigate(CityNavRoutes.bentoGuide(cityId)) }
                )
            }
        }

        // ── City Detail ────────────────────────────────────────────────────
        composable(
            route     = CityNavRoutes.CITY_DETAIL,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cityId   = backStackEntry.arguments?.getString("cityId") ?: return@composable
            val cityData = remember(cityId) { CityRepository.getInstance().getCityById(context, cityId) }

            if (cityData != null) {
                CityDetailScreen(
                    cityData     = cityData,
                    points       = 0,
                    onBack       = { navController.popBackStack() },
                    onMapClick   = {
                        val coords = cityData.mapCoords ?: ""
                        val cName  = cityData.name ?: "Romania"
                        val uri    = android.net.Uri.parse("geo:$coords?q=$cName,Romania")
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        try { context.startActivity(intent) } catch (e: Exception) {
                            intent.setPackage(null)
                            try { context.startActivity(intent) } catch (ignored: Exception) {}
                        }
                    },
                    onPhotoClick = { /* open image picker */ }
                )
            }
        }

        // ── Bento Guide ────────────────────────────────────────────────────
        composable(
            route     = CityNavRoutes.BENTO_GUIDE,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) {
            BentoGuideScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────
private fun buildCategories(cityData: CityData): List<CategoryUi> {
    val sections = cityData.sections ?: return emptyList()
    val icons = listOf("📷", "🎵", "🎓", "🌿", "🍽️", "🏛️")
    return sections.mapIndexed { i, sec ->
        CategoryUi(
            id          = sec.title ?: i.toString(),
            title       = sec.title ?: "Secțiune",
            description = sec.content?.take(100) ?: "",
            imageUrl    = "https://picsum.photos/seed/${cityData.id}-$i/400/300",
            icon        = icons.getOrElse(i) { "📍" }
        )
    }
}
