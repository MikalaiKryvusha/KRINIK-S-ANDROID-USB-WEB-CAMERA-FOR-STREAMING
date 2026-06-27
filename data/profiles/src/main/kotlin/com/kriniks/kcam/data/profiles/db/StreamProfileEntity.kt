/**
 * StreamProfileEntity — Room table row for a streaming platform profile.
 * Maps 1:1 to StreamProfile domain model via toProfile() / StreamProfile.toEntity().
 * Related: AppDatabase, StreamProfileDao, StreamProfile (domain model)
 */

package com.kriniks.kcam.data.profiles.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kriniks.kcam.data.profiles.model.StreamPlatform
import com.kriniks.kcam.data.profiles.model.StreamProfile

@Entity(tableName = "stream_profiles")
data class StreamProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val platform: String,           // StreamPlatform.name()
    val rtmpUrl: String,
    val streamKey: String,
    val isEnabled: Boolean,
    val videoWidth: Int,
    val videoHeight: Int,
    val videoFps: Int,
    val videoBitrateBps: Int,
) {
    fun toProfile() = StreamProfile(
        id             = id,
        name           = name,
        platform       = StreamPlatform.valueOf(platform),
        rtmpUrl        = rtmpUrl,
        streamKey      = streamKey,
        isEnabled      = isEnabled,
        videoWidth     = videoWidth,
        videoHeight    = videoHeight,
        videoFps       = videoFps,
        videoBitrateBps = videoBitrateBps,
    )
}

fun StreamProfile.toEntity() = StreamProfileEntity(
    id              = id,
    name            = name,
    platform        = platform.name,
    rtmpUrl         = rtmpUrl,
    streamKey       = streamKey,
    isEnabled       = isEnabled,
    videoWidth      = videoWidth,
    videoHeight     = videoHeight,
    videoFps        = videoFps,
    videoBitrateBps = videoBitrateBps,
)
