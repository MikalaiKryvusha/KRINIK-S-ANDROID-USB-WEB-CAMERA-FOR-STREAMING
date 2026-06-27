/**
 * CodecInfo — capabilities of a single encoder discovered via MediaCodecList.
 *
 * Populated by CodecScanner on first launch.
 * Stored in DeviceProfile to avoid re-scanning on every start.
 *
 * Related: CodecScanner, DeviceProfile (:data:profiles)
 */

package com.kriniks.kcam.feature.codec.model

data class CodecInfo(
    val name: String,               // e.g. "OMX.MTK.VIDEO.ENCODER.AVC"
    val mimeType: String,           // e.g. "video/avc", "video/hevc", "video/av01"
    val isHardwareAccelerated: Boolean,
    val maxWidth: Int,
    val maxHeight: Int,
    val maxFps: Int,
    val maxBitrateBps: Int,
) {
    val isH264: Boolean get() = mimeType == "video/avc"
    val isHevc: Boolean get() = mimeType == "video/hevc"
    val isAv1: Boolean  get() = mimeType == "video/av01"

    /** Returns a short readable label for logging / UI */
    val label: String
        get() = "${if (isHardwareAccelerated) "HW" else "SW"} ${mimeType.substringAfterLast('/')}" +
                " ${maxWidth}x${maxHeight}@${maxFps}fps ${maxBitrateBps / 1_000_000}Mbps"
}
