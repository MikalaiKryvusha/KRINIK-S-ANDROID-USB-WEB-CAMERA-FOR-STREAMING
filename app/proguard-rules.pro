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
