/**
 * StreamViewModel — UI state for the streaming feature.
 *
 * Exposes:
 *   streamState — current StreamState (Idle / Connecting / Live / Error)
 *   profiles    — list of all configured streaming platforms
 *   activeProfile — the profile selected for the next / current stream
 *
 * Actions:
 *   startStream(profile) — validate + start RTMP
 *   stopStream()         — graceful stop
 *   saveProfile(profile) — add/edit a platform profile
 *   deleteProfile(p)     — remove a platform profile
 *
 * Related: StreamingRepository, RtmpStreamer, StreamPlatformsOverlay (UI)
 */

package com.kriniks.kcam.feature.streaming.ui

import android.view.TextureView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kriniks.kcam.core.logging.KLog
import com.kriniks.kcam.data.profiles.model.StreamProfile
import com.kriniks.kcam.feature.streaming.domain.StreamingRepository
import com.kriniks.kcam.feature.streaming.model.StreamState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StreamViewModel"

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val repository: StreamingRepository,
) : ViewModel() {

    val streamState: StateFlow<StreamState> = repository.streamState

    val profiles: StateFlow<List<StreamProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _activeProfile = MutableStateFlow<StreamProfile?>(null)
    val activeProfile: StateFlow<StreamProfile?> = _activeProfile.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    // Set automatically when profiles load — pick first enabled profile
    init {
        viewModelScope.launch {
            repository.enabledProfiles.collect { list ->
                if (_activeProfile.value == null) {
                    _activeProfile.value = list.firstOrNull()
                }
            }
        }
    }

    fun attachPreviewSurface(textureView: TextureView) {
        repository.attachPreviewSurface(textureView)
    }

    fun startStream() {
        val profile = _activeProfile.value
        if (profile == null) {
            viewModelScope.launch { _snackbar.emit("No streaming platform configured") }
            return
        }
        KLog.i(TAG, "Starting stream on profile '${profile.name}'")
        val ok = repository.startStream(profile)
        if (!ok) {
            viewModelScope.launch { _snackbar.emit("Failed to start encoder — check device support") }
        }
    }

    fun stopStream() {
        KLog.i(TAG, "Stopping stream")
        repository.stopStream()
    }

    fun selectProfile(profile: StreamProfile) {
        _activeProfile.value = profile
    }

    fun saveProfile(profile: StreamProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            KLog.i(TAG, "Saved profile '${profile.name}'")
        }
    }

    fun deleteProfile(profile: StreamProfile) {
        viewModelScope.launch {
            if (profile == _activeProfile.value) _activeProfile.value = null
            repository.deleteProfile(profile)
        }
    }
}
