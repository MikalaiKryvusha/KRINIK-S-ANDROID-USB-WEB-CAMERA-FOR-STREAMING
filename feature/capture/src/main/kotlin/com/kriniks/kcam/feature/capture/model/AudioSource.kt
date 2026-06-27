/**
 * AudioSource — sealed interface for all audio input sources.
 *
 * Phase 1: PhoneMic only. Phase 2+: UvcMic (USB camera's built-in mic).
 * DeviceManager lists all available sources so the user can pick one.
 * Related: VideoSource, DeviceManager
 */

package com.kriniks.kcam.feature.capture.model

import android.media.AudioDeviceInfo

sealed interface AudioSource {
    val id: String
    val displayName: String
    val isAvailable: Boolean

    /** Built-in microphone of the phone / tablet */
    data class PhoneMic(
        val audioDeviceId: Int = AudioDeviceInfo.TYPE_BUILTIN_MIC,
    ) : AudioSource {
        override val id = "phone_mic"
        override val displayName = "Phone microphone"
        override val isAvailable = true
    }

    /** Microphone embedded in a UVC USB camera (USB Audio Class) */
    data class UvcMic(
        override val id: String,
        override val displayName: String,
        val usbDeviceId: Int,
    ) : AudioSource {
        override val isAvailable = true
    }

    /** Silence — stream without audio */
    object None : AudioSource {
        override val id = "none"
        override val displayName = "No audio"
        override val isAvailable = true
    }
}
