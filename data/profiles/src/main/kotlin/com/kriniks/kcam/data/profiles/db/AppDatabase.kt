/**
 * AppDatabase — Room database for KrinikCam.
 *
 * Currently holds: stream_profiles table.
 * Future tables: device_profiles (Phase 3), overlay_presets (Phase 6).
 *
 * Version history:
 *   1 — initial schema (stream_profiles)
 *
 * Related: StreamProfileDao, ProfilesModule (Hilt), DeviceProfile (DataStore, not Room)
 */

package com.kriniks.kcam.data.profiles.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StreamProfileEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun streamProfileDao(): StreamProfileDao
}
