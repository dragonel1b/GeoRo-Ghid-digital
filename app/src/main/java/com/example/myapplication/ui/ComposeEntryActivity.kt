package com.example.myapplication.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.navigation.CityNavGraph
import com.example.myapplication.ui.theme.ClujTheme

/**
 * Unified entry point for the modern data-driven city dashboard.
 *
 * This Activity replaces the legacy city activities by providing a unified
 * dynamic UI powered by Jetpack Compose.
 *
 * Intent Extras:
 *   - EXTRA_CITY_ID: The normalized ID of the city (e.g., "cluj-napoca", "suceava").
 */
class ComposeEntryActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CITY_ID = "CITY_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cityId = intent.getStringExtra(EXTRA_CITY_ID) ?: "cluj-napoca"

        setContent {
            ClujTheme {
                CityNavGraph(
                    startCityId = cityId,
                )
            }
        }
    }
}
