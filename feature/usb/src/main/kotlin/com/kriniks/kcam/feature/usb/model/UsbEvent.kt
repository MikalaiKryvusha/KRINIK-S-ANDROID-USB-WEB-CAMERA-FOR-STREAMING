/**
 * UsbEvent — all USB camera lifecycle events emitted as a Flow.
 * Consumed by UsbViewModel and DeviceManager.
 * Related: UsbDeviceRepository, UvcCameraManager
 */

package com.kriniks.kcam.feature.usb.model

import android.hardware.usb.UsbDevice
import com.jiangdg.ausbc.MultiCameraClient

sealed class UsbEvent {
    /** A UVC device was attached to the USB port */
    data class DeviceAttached(val device: UsbDevice) : UsbEvent()

    /** A previously attached device was removed */
    data class DeviceDetached(val deviceId: Int) : UsbEvent()

    /**
     * USB permission granted and camera object is ready to be opened.
     * The camera is NOT yet opened at this point — UvcPreviewView calls openCamera()
     * when a TextureView is available.
     */
    data class PermissionGranted(
        val device: UsbDevice,
        val camera: MultiCameraClient.Camera,
    ) : UsbEvent()

    /** User denied USB access permission */
    data class PermissionDenied(val device: UsbDevice) : UsbEvent()

    /** Camera preview started on this device (fired after camera.openCamera completes) */
    data class PreviewStarted(val deviceId: Int, val width: Int, val height: Int) : UsbEvent()

    /** Camera preview stopped or failed */
    data class PreviewStopped(val deviceId: Int) : UsbEvent()

    /** An error occurred in the USB camera subsystem */
    data class Error(val message: String, val cause: Throwable? = null) : UsbEvent()
}
