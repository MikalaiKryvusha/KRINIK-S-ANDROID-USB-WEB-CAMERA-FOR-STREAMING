package com.kriniks.kcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kriniks.kcam.core.logging.FileLogger
import com.kriniks.kcam.core.ui.theme.KrinikCamTheme
import com.kriniks.kcam.feature.capture.DeviceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single entry-point activity for the entire app.
 * Uses Jetpack Compose + single NavHost (KrinikCamNavGraph) as the UI backbone.
 * All screens are composable destinations — no Fragment transactions.
 *
 * Injects DeviceManager and FileLogger so they can be passed down to screens
 * without threading them through ViewModels.
 *
 * Related: KrinikCamNavGraph, MainScreen, KrinikCamApp
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var deviceManager: DeviceManager
    @Inject lateinit var fileLogger: FileLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KrinikCamTheme {
                KrinikCamNavGraph(
                    deviceManager = deviceManager,
                    fileLogger = fileLogger,
                )
            }
        }
    }
}
