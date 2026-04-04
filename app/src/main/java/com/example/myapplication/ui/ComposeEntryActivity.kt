package com.example.myapplication.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.myapplication.RomApp.CityDetailActivity
import com.example.myapplication.ui.navigation.CityNavGraph
import com.example.myapplication.ui.theme.ClujTheme

/**
 * Entry point for the Compose city screens.
 *
 * Launch this Activity instead of [CityDetailActivity] for cities
 * that should show the new Compose UI. Receives the same Intent extra:
 *   Intent.putExtra(EXTRA_CITY_ID, cityId)
 *
 * All legacy Java activities remain untouched (Opțiunea A — Hibrid).
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
