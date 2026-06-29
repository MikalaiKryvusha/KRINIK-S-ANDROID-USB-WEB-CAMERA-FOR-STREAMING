/**
 * RotatableSource — a VideoSource that rotates its OWN frame (Bug 10, variant C).
 *
 * Why: RootEncoder's encoder-side rotation (prepareVideo rotation / setStreamRotation) applies a
 * NON-uniform scale (SizeCalculator.getScale) tuned for real cameras; for our custom Surface-based
 * sources (VirtualVideoSource, UvcVideoSource) it distorts (oval). The simple, KISS fix (per Krinik's
 * model — "WE virtually rotate the incoming 16:9 stream") is to do the rotation ourselves IN THE
 * SOURCE: draw the landscape 16:9 frame geometrically rotated into a portrait 1080×1920 buffer, and
 * leave the encoder rotation at 0 (so getScale returns 1,1 → no distortion).
 *
 * The streamer calls [setOutputRotation] before prepareVideo so the source knows which way to rotate.
 *
 * Related: RtmpStreamer.startStream/startRecordToFile, VirtualVideoSource, bugs/10_*.md
 */
package com.kriniks.kcam.feature.streaming.rtmp

interface RotatableSource {
    /** Desired output rotation in degrees: 0 / 90 / 180 / 270. The source rotates its frame so the
     * encoder receives an already-upright portrait (90/270) or landscape (0/180) frame. */
    fun setOutputRotation(degrees: Int)
}
