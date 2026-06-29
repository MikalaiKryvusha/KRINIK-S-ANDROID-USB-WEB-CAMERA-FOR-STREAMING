/**
 * FreezeStandbyVideoSource — graceful camera-dropout handling (Interview #004).
 *
 * When the video source disconnects mid-stream we DON'T jump straight to the "Please stand by" card.
 * Instead we FREEZE on the last live frame for [holdMs] (default 5s): if the camera comes back within
 * that window the streamer swaps back to the live source and the viewer never sees a glitch. Only if
 * the source stays gone past [holdMs] do we cross-fade (over [fadeMs]) into the standby card.
 *
 * Mechanism mirrors StandbyVideoSource: wrap the encoder's GL input SurfaceTexture in a producer
 * Surface and draw with a software Canvas on a HandlerThread, so the encoder stays fed (RTMP alive).
 * The only difference is WHAT we draw, decided by elapsed time:
 *   • elapsed < holdMs              → the frozen last frame
 *   • holdMs ≤ elapsed < holdMs+fadeMs → frozen frame + standby card cross-fading in (alpha ramp)
 *   • elapsed ≥ holdMs+fadeMs       → the standby card (steady; == StandbyVideoSource from here on)
 *
 * Returning to live within holdMs = RtmpStreamer.exitStandby() (instant, per Interview #004 Q2 v1).
 *
 * Related: StandbyVideoSource (the post-timeout state), StandbyFrameRenderer, RtmpStreamer.enterStandby
 */

package com.kriniks.kcam.feature.streaming.rtmp

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.Surface
import com.kriniks.kcam.core.logging.KLog
import com.pedro.library.util.sources.video.VideoSource

/**
 * A VideoSource that can hand back its last rendered frame, so RtmpStreamer.enterStandby can FREEZE
 * on it (Interview #004). Reading from the source is reliable even when the preview TextureView is
 * already being torn down by the UI on a source drop (TextureView.getBitmap() returns null then).
 */
interface LastFrameProvider {
    fun lastFrame(): Bitmap?
}

private const val TAG = "FreezeStandbyVideoSource"

// 10 fps: a static freeze needs little; 10fps still gives the 500ms cross-fade ~5 steps while keeping
// the encoder light enough for heavy profiles (e.g. 4K) so we don't starve it. Interview #004 timings:
// hold 5000ms, fade 500ms.
private const val FREEZE_FPS = 10L
private const val FRAME_INTERVAL_MS = 1000L / FREEZE_FPS

class FreezeStandbyVideoSource(
    private val frozenFrame: Bitmap,
    private val standbyBitmap: Bitmap,
    private val holdMs: Long = 5000L,
    private val fadeMs: Long = 500L,
) : VideoSource() {

    private var surface: Surface? = null
    private var drawThread: HandlerThread? = null
    private var drawHandler: Handler? = null
    @Volatile private var running = false
    private var startMs = 0L
    private val fadePaint = Paint(Paint.FILTER_BITMAP_FLAG)

    override fun create(width: Int, height: Int, fps: Int, rotation: Int): Boolean = true

    override fun start(surfaceTexture: SurfaceTexture) {
        val w = if (width > 0) width else standbyBitmap.width
        val h = if (height > 0) height else standbyBitmap.height
        try {
            surfaceTexture.setDefaultBufferSize(w, h)
            surface = Surface(surfaceTexture)
            running = true
            startMs = SystemClock.elapsedRealtime()

            val thread = HandlerThread("FreezeStandbyDraw").also { it.start() }
            val handler = Handler(thread.looper)
            drawThread = thread
            drawHandler = handler

            val dst = Rect(0, 0, w, h)
            val srcFrozen = Rect(0, 0, frozenFrame.width, frozenFrame.height)
            val srcStandby = Rect(0, 0, standbyBitmap.width, standbyBitmap.height)

            val loop = object : Runnable {
                override fun run() {
                    if (!running) return
                    drawOnce(srcFrozen, srcStandby, dst)
                    if (running) handler.postDelayed(this, FRAME_INTERVAL_MS)
                }
            }
            handler.post(loop)
            KLog.d(TAG, "Freeze-standby started — hold ${holdMs}ms then fade ${fadeMs}ms into standby (${w}x${h})")
        } catch (e: Exception) {
            KLog.e(TAG, "Failed to start freeze-standby source", e)
            running = false
        }
    }

    private fun drawOnce(srcFrozen: Rect, srcStandby: Rect, dst: Rect) {
        val s = surface ?: return
        try {
            val canvas = s.lockCanvas(null) ?: return
            val elapsed = SystemClock.elapsedRealtime() - startMs
            when {
                elapsed < holdMs -> {
                    // Hold the last live frame — a brief dropout looks like a frozen picture.
                    canvas.drawBitmap(frozenFrame, srcFrozen, dst, null)
                }
                elapsed < holdMs + fadeMs -> {
                    // Cross-fade frozen frame → standby card over fadeMs.
                    val t = (elapsed - holdMs).toFloat() / fadeMs  // 0f → 1f
                    canvas.drawBitmap(frozenFrame, srcFrozen, dst, null)
                    fadePaint.alpha = (t * 255f).toInt().coerceIn(0, 255)
                    canvas.drawBitmap(standbyBitmap, srcStandby, dst, fadePaint)
                }
                else -> {
                    // Source still gone — steady standby card (same as StandbyVideoSource).
                    canvas.drawBitmap(standbyBitmap, srcStandby, dst, null)
                }
            }
            s.unlockCanvasAndPost(canvas)
        } catch (e: Exception) {
            KLog.w(TAG, "Freeze-standby draw failed: ${e.message}")
        }
    }

    override fun stop() {
        running = false
        drawHandler?.removeCallbacksAndMessages(null)
        drawThread?.quitSafely()
        drawThread = null
        drawHandler = null
        surface?.release()
        surface = null
        KLog.d(TAG, "Freeze-standby source stopped")
    }

    override fun release() = stop()

    override fun isRunning(): Boolean = running
}
