/**
 * KLog — unified logging API for the entire KrinikCam app.
 *
 * Wraps Timber for console output and routes to FileLogger for persistent
 * log files that can be shared via the debug share action.
 *
 * Usage from any module:
 *   KLog.d("UsbCamera", "Device opened: $deviceName")
 *   KLog.e("Streaming", "RTMP connect failed", exception)
 *
 * Related: FileLogger (file sink), LoggingModule (Hilt binding)
 */

package com.kriniks.kcam.core.logging

import timber.log.Timber

object KLog {

    // Set by LoggingModule during app init — keeps KLog decoupled from Context
    internal var fileLogger: FileLogger? = null

    fun d(tag: String, msg: String) {
        Timber.tag(tag).d(msg)
        fileLogger?.log(LogLevel.DEBUG, tag, msg)
    }

    fun i(tag: String, msg: String) {
        Timber.tag(tag).i(msg)
        fileLogger?.log(LogLevel.INFO, tag, msg)
    }

    fun w(tag: String, msg: String, t: Throwable? = null) {
        if (t != null) Timber.tag(tag).w(t, msg) else Timber.tag(tag).w(msg)
        fileLogger?.log(LogLevel.WARN, tag, msg, t)
    }

    fun e(tag: String, msg: String, t: Throwable? = null) {
        if (t != null) Timber.tag(tag).e(t, msg) else Timber.tag(tag).e(msg)
        fileLogger?.log(LogLevel.ERROR, tag, msg, t)
    }
}

enum class LogLevel { DEBUG, INFO, WARN, ERROR }
