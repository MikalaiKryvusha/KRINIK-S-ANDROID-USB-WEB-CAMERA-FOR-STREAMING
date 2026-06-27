/**
 * RtmpStreamer — RTMP streaming engine wrapping RootEncoder's RtmpCamera1.
 *
 * Connects a TextureView (from :feature:usb) as video source and uses
 * MediaCodec H.264 hardware encoding. Audio comes from the phone microphone (Phase 1).
 *
 * Phase 1 supports a single RTMP destination. MultiStreamManager in Phase 2
 * will extend this to N simultaneous destinations.
 *
 * When the video source disconnects, sendStandbyFrame() injects a "Please stand by"
 * bitmap frame so the RTMP session stays alive.
 *
 * RootEncoder 2.4.7 API differences from older versions:
 *   - Package: com.pedro.library.rtmp (not com.pedro.rtplibrary.rtmp)
 *   - ConnectChecker: com.pedro.common.ConnectChecker (not ConnectCheckerRtmp)
 *   - Callbacks: onConnectionSuccess/onConnectionFailed (no "Rtmp" suffix)
 *   - prepareVideo: last param is rotation (int), not CameraHelper.Facing
 *
 * Related: StreamState, StreamViewModel, UvcPreviewView (:feature:usb),
 *          StandbyPlaceholder (:app), StreamProfile (:data:profiles)
 */

package com.kriniks.kcam.feature.streaming.rtmp

import android.content.Context
import android.graphics.Bitmap  // kept for Phase 2 sendStandbyFrame param
import android.view.TextureView
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1
import com.kriniks.kcam.core.logging.KLog
import com.kriniks.kcam.data.profiles.model.StreamProfile
import com.kriniks.kcam.feature.streaming.model.StreamState
import com.kriniks.kcam.feature.streaming.model.isActive
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RtmpStreamer"

@Singleton
class RtmpStreamer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _state = MutableStateFlow<StreamState>(StreamState.Idle)
    val state: StateFlow<StreamState> = _state.asStateFlow()

    private var rtmpCamera: RtmpCamera1? = null

    // ── ConnectChecker — RootEncoder 2.4.7 connection callbacks ───────────

    private val connectChecker = object : ConnectChecker {
        override fun onConnectionStarted(url: String) {
            KLog.i(TAG, "RTMP connecting → $url")
        }

        override fun onConnectionSuccess() {
            KLog.i(TAG, "RTMP connected")
            _state.value = StreamState.Live()
        }

        override fun onConnectionFailed(reason: String) {
            KLog.e(TAG, "RTMP connection failed: $reason")
            _state.value = StreamState.Error(reason)
            rtmpCamera?.stopStream()
        }

        override fun onNewBitrate(bitrate: Long) {
            val current = _state.value
            if (current is StreamState.Live) {
                _state.value = current.copy(bitrateKbps = (bitrate / 1000).toInt())
            }
        }

        override fun onDisconnect() {
            KLog.w(TAG, "RTMP disconnected")
            if (_state.value.isActive) {
                _state.value = StreamState.Error("Disconnected from server")
            }
        }

        override fun onAuthError() {
            KLog.e(TAG, "RTMP auth error")
            _state.value = StreamState.Error("Authentication failed — check stream key")
        }

        override fun onAuthSuccess() {
            KLog.i(TAG, "RTMP auth OK")
        }
    }

    /**
     * Attach the preview TextureView. Must be called before startStream().
     * RootEncoder uses the TextureView's SurfaceTexture as encoder input.
     */
    fun attachTextureView(tv: TextureView) {
        rtmpCamera = RtmpCamera1(tv, connectChecker)
        KLog.d(TAG, "TextureView attached to RtmpStreamer")
    }

    /**
     * Start RTMP stream to the given profile.
     * Returns false if the stream couldn't be prepared (codec issue).
     */
    fun startStream(profile: StreamProfile): Boolean {
        val camera = rtmpCamera ?: run {
            KLog.e(TAG, "startStream called before attachTextureView")
            return false
        }

        if (camera.isStreaming) {
            KLog.w(TAG, "Already streaming — ignoring startStream")
            return true
        }

        val rtmpUrl = "${profile.rtmpUrl}/${profile.streamKey}"
        KLog.i(TAG, "Starting RTMP stream → $rtmpUrl")

        // rotation=0 — UVC webcam frames are already in the correct orientation
        val prepared = camera.prepareVideo(
            profile.videoWidth,
            profile.videoHeight,
            profile.videoFps,
            profile.videoBitrateBps,
            0,  // rotation
        ) && camera.prepareAudio(
            128_000,  // 128kbps AAC
            44100,    // 44.1 kHz
            true,     // stereo
        )

        return if (prepared) {
            _state.value = StreamState.Connecting
            camera.startStream(rtmpUrl)
            true
        } else {
            val msg = "Failed to prepare encoder — check device codec support"
            KLog.e(TAG, msg)
            _state.value = StreamState.Error(msg)
            false
        }
    }

    fun stopStream() {
        KLog.i(TAG, "Stopping RTMP stream")
        _state.value = StreamState.Stopping
        rtmpCamera?.stopStream()
        _state.value = StreamState.Idle
    }

    /**
     * Feed a "Please stand by" bitmap when USB camera disconnects mid-stream.
     * Phase 1: no-op — RootEncoder 2.4.7 removed setCustomImageToStream();
     * Phase 2 will implement this via BaseFilterRender with a static image filter.
     */
    fun sendStandbyFrame(bitmap: Bitmap) {
        KLog.d(TAG, "sendStandbyFrame: standby image injection not yet implemented (Phase 2)")
    }

    fun clearStandbyFrame() {
        // Phase 2: remove standby filter from GlInterface
    }

    val isStreaming: Boolean get() = rtmpCamera?.isStreaming == true
}
