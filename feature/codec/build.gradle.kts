// :feature:codec — MediaCodec scanner: discovers available HW/SW codecs and their
// capabilities (max resolution, FPS, bitrate). Used by :feature:streaming to pick
// the best encoder. Foundational module.
// Related: CodecScanner, CodecInfo, CodecModule

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kriniks.kcam.feature.codec"
    compileSdk = 35

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:logging"))
    implementation(project(":data:profiles"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.coroutines.android)
}
