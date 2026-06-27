package com.kriniks.kcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kriniks.kcam.core.ui.theme.KrinikCamTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single entry-point activity for the entire app.
 * Uses Jetpack Compose + single NavHost pattern — all screens are composable destinations.
 *
 * Marked with @AndroidEntryPoint to allow Hilt injection into this activity
 * and all composables within its composition tree.
 *
 * Related: KrinikCamTheme (core:ui), NavHost destinations added in Phase 1.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make content draw behind system bars (status bar + navigation bar)
        enableEdgeToEdge()

        setContent {
            KrinikCamTheme {
                // TODO Phase 1: replace with AppNavHost composable
            }
        }
    }
}
