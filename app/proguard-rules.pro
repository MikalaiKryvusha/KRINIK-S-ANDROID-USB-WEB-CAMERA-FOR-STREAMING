# KrinikCam ProGuard rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# Keep Room entities
-keep class com.kriniks.kcam.**.entity.** { *; }

# Keep data classes used for serialization
-keep @kotlinx.serialization.Serializable class * { *; }

# Timber
-dontwarn org.jetbrains.annotations.**

# ─────────────────────────────────────────────────────────────────────────────
# Native (JNI) libraries — MUST NOT be renamed/stripped by R8 (Bug 05)
# ─────────────────────────────────────────────────────────────────────────────
# The USB camera stack loads native .so libraries that bind to Java methods by
# their EXACT class + method signatures at System.loadLibrary() time (JNI
# RegisterNatives). If R8 renames or removes those methods/classes, the native
# binding fails with NoSuchMethodError and the release build crashes the instant
# the camera starts — e.g. UVCCamera.nativeSetStatusCallback(...IStatusCallback).
# Debug builds don't minify, so the crash is release-only. Keep them verbatim.

# Never rename native methods anywhere (keeps JNI signatures intact).
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# serenegiant USB/UVC native stack (libUVCCamera / libuvc) + its JNI callbacks
# (IStatusCallback, IButtonCallback, IFrameCallback live under this package).
-keep class com.serenegiant.** { *; }
-dontwarn com.serenegiant.**

# AndroidUSBCamera (AUSBC) — wraps serenegiant, uses reflection for camera setup.
-keep class com.jiangdg.** { *; }
-dontwarn com.jiangdg.**

# RootEncoder (RTMP/SRT) — native transport + reflection-based codec selection.
-keep class com.pedro.** { *; }
-dontwarn com.pedro.**
