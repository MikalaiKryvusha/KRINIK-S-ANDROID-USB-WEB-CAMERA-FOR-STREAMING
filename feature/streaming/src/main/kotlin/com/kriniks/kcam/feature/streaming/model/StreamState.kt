/**
 * StreamState — current state of the RTMP streaming session.
 * Observed by StreamViewModel and the UI overlay.
 * Related: RtmpStreamer, StreamViewModel, FloatingRadialMenu
 */

package com.kriniks.kcam.feature.streaming.model

sealed class StreamState {
    object Idle : StreamState()                     // not streaming
    object Connecting : StreamState()               // TCP handshake in progress
    data class Live(                                // streaming successfully
        val durationMs: Long = 0,
        val bitrateKbps: Int = 0,
        val droppedFrames: Int = 0,
    ) : StreamState()
    data class Error(val message: String) : StreamState()   // connection lost
    object Stopping : StreamState()                 // stopping in progress
}

val StreamState.isLive: Boolean get() = this is StreamState.Live
val StreamState.isActive: Boolean get() = this is StreamState.Live || this is StreamState.Connecting
