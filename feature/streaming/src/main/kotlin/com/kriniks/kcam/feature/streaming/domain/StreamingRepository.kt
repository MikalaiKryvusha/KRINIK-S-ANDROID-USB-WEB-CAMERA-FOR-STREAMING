/**
 * StreamingRepository — business logic layer between ViewModel and RtmpStreamer.
 *
 * Loads stream profiles from :data:profiles, validates them, and delegates
 * to RtmpStreamer for the actual encoding + transport.
 *
 * Related: RtmpStreamer, ProfilesRepository (:data:profiles), StreamViewModel
 */

package com.kriniks.kcam.feature.streaming.domain

import android.view.TextureView
import com.kriniks.kcam.core.logging.KLog
import com.kriniks.kcam.data.profiles.model.StreamProfile
import com.kriniks.kcam.data.profiles.repository.ProfilesRepository
import com.kriniks.kcam.feature.streaming.model.StreamState
import com.kriniks.kcam.feature.streaming.rtmp.RtmpStreamer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StreamingRepository"

@Singleton
class StreamingRepository @Inject constructor(
    private val rtmpStreamer: RtmpStreamer,
    private val profilesRepository: ProfilesRepository,
) {
    val streamState: StateFlow<StreamState> = rtmpStreamer.state
    val allProfiles: Flow<List<StreamProfile>> = profilesRepository.observeAllProfiles()
    val enabledProfiles: Flow<List<StreamProfile>> = profilesRepository.observeEnabledProfiles()

    fun attachPreviewSurface(textureView: TextureView) {
        rtmpStreamer.attachTextureView(textureView)
    }

    fun startStream(profile: StreamProfile): Boolean {
        if (profile.streamKey.isBlank()) {
            KLog.w(TAG, "Stream key is empty for profile '${profile.name}'")
            return false
        }
        return rtmpStreamer.startStream(profile)
    }

    fun stopStream() = rtmpStreamer.stopStream()

    suspend fun saveProfile(profile: StreamProfile) = profilesRepository.saveProfile(profile)

    suspend fun deleteProfile(profile: StreamProfile) = profilesRepository.deleteProfile(profile)
}
