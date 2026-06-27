/**
 * DeviceProfile — capabilities snapshot of the current Android device.
 *
 * Auto-populated on first launch by :feature:codec scanner.
 * Used by :feature:streaming to select optimal encoder settings.
 * Stored in DataStore (not Room — device profile is a single object, not a list).
 *
 * Related: ProfilesDataStore, CodecScanner (:feature:codec)
 */

package com.kriniks.kcam.data.profiles.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceProfile(
    val deviceModel: String = "",
    val deviceSoc: String = "",           // e.g. "Dimensity 8300"
    val hasHwH264: Boolean = false,
    val hasHwHevc: Boolean = false,
    val hasHwAv1: Boolean = false,
    val maxH264WidthPx: Int = 1920,
    val maxH264HeightPx: Int = 1080,
    val maxH264FPS: Int = 30,
    val maxH264BitrateBps: Int = 4_000_000,
    val preferredCodecMime: String = "video/avc",  // video/avc = H.264
)
