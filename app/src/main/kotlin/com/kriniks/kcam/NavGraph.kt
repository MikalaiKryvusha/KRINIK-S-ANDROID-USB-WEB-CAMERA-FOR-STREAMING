/**
 * NavGraph — Compose Navigation graph for KrinikCam.
 *
 * Phase 1 routes:
 *   main     → MainScreen (fullscreen viewfinder)
 *   settings → SettingsScreen
 *
 * Future routes (Phase 4+):
 *   device_manager → DeviceManagerScreen
 *   profiles       → ProfilesScreen
 *
 * Related: MainActivity, MainScreen, SettingsScreen
 */

package com.kriniks.kcam

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kriniks.kcam.core.logging.FileLogger
import com.kriniks.kcam.feature.capture.DeviceManager
import com.kriniks.kcam.ui.screens.MainScreen
import com.kriniks.kcam.ui.screens.SettingsScreen

object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
}

@Composable
fun KrinikCamNavGraph(
    deviceManager: DeviceManager,
    fileLogger: FileLogger,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                deviceManager = deviceManager,
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                fileLogger = fileLogger,
            )
        }
    }
}
