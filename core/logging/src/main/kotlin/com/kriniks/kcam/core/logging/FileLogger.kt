/**
 * FileLogger — writes log lines to a rotating daily file on external storage.
 *
 * Log location: <app-external-files>/logs/kcam_YYYY-MM-DD.log
 * Retention: 7 days (older files deleted on init).
 * Format: [HH:mm:ss.SSS] [LEVEL] [TAG] message (\n throwable stacktrace if present)
 *
 * The log file can be shared via ShareLogAction — useful for remote debugging.
 * Related: KLog (caller), ShareLogAction (share intent), LoggingModule (init)
 */

package com.kriniks.kcam.core.logging

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileLogger @Inject constructor(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val logDir: File
        get() = File(context.getExternalFilesDir(null), "logs").also { it.mkdirs() }

    private val currentLogFile: File
        get() = File(logDir, "kcam_${dateFormat.format(Date())}.log")

    init {
        pruneOldLogs()
    }

    fun log(level: LogLevel, tag: String, msg: String, t: Throwable? = null) {
        scope.launch {
            try {
                val time = timeFormat.format(Date())
                val line = "[$time] [${level.name.padEnd(5)}] [$tag] $msg\n"
                currentLogFile.appendText(line)
                t?.let { currentLogFile.appendText(it.stackTraceToString() + "\n") }
            } catch (_: Exception) {
                // Never crash the app due to logging failure
            }
        }
    }

    /** Returns a content:// URI suitable for sharing via Intent.ACTION_SEND */
    fun shareIntent(): Intent {
        val file = currentLogFile
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "KrinikCam log ${dateFormat.format(Date())}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /** Delete log files older than 7 days */
    private fun pruneOldLogs() {
        scope.launch {
            val cutoff = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            logDir.listFiles()
                ?.filter { it.lastModified() < cutoff }
                ?.forEach { it.delete() }
        }
    }
}
