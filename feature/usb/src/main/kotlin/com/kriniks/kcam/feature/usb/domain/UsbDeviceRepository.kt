/**
 * UsbDeviceRepository — interface for USB camera management.
 * Decouples consumers (:feature:streaming, :app) from the AndroidUSBCamera implementation.
 * Related: UsbDeviceRepositoryImpl, UsbEvent, UsbModule
 */

package com.kriniks.kcam.feature.usb.domain

import android.hardware.usb.UsbDevice
import com.jiangdg.ausbc.MultiCameraClient
import com.kriniks.kcam.feature.usb.model.UsbEvent
import kotlinx.coroutines.flow.SharedFlow

interface UsbDeviceRepository {
    val events: SharedFlow<UsbEvent>
    fun startMonitoring()
    fun stopMonitoring()
    fun requestPermission(device: UsbDevice)
    fun getCameraForDevice(deviceId: Int): MultiCameraClient.Camera?
}
