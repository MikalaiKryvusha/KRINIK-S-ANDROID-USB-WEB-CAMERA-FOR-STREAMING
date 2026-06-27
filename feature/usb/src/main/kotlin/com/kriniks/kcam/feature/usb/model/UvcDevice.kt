/**
 * UvcDevice — domain model for a connected UVC USB camera.
 *
 * Populated from android.hardware.usb.UsbDevice after permission is granted.
 * videoProfiles is queried from the camera via USB Video Class descriptors
 * (provided by AndroidUSBCamera library).
 *
 * Related: UsbEvent, UvcCameraManager, VideoSource.UvcCamera
 */

package com.kriniks.kcam.feature.usb.model

data class UvcDevice(
    val deviceId: Int,
    val vendorId: Int,
    val productId: Int,
    val displayName: String,
    val videoProfiles: List<UvcVideoProfile> = emptyList(),
)

/** A single native video profile reported by the camera over USB */
data class UvcVideoProfile(
    val width: Int,
    val height: Int,
    val fps: Int,
    val format: UvcFormat,
) {
    val label: String get() = "${width}x${height}@${fps}fps ${format.name}"
}

enum class UvcFormat { MJPEG, YUV, H264 }
