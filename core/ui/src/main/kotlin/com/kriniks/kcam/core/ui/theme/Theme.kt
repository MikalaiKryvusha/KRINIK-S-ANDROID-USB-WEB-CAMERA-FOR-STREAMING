package com.kriniks.kcam.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * KrinikCam Material3 colour schemes.
 *
 * Dark scheme is the primary design target — streaming apps are typically used in
 * dim environments and the dark background maximises camera preview visibility.
 *
 * Dynamic colour (Android 12+ Material You) is supported optionally — users who
 * prefer it get their wallpaper colours, others get the KrinikCam brand palette.
 */
private val DarkColorScheme = darkColorScheme(
    primary = AcidPink,
    onPrimary = DarkOnBackground,
    primaryContainer = AcidPinkDark,
    onPrimaryContainer = AcidPinkLight,
    secondary = AcidPinkLight,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurface,
    error = ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary = AcidPinkDark,
    onPrimary = LightBackground,
    primaryContainer = AcidPinkLight,
    onPrimaryContainer = AcidPinkDark,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightSurfaceVariant,
    error = ErrorRed,
)

/**
 * Root Compose theme for the entire KrinikCam app.
 *
 * Applied at the top level in MainActivity, so all composable screens
 * automatically inherit the brand colours, typography, and shapes.
 *
 * @param darkTheme  defaults to system preference; streaming screens may force dark
 * @param dynamicColor  use Material You (Android 12+); falls back to brand colours on older OS
 */
@Composable
fun KrinikCamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KrinikCamTypography,
        content = content
    )
}
