// :core:logging — file-based debug logger used by every other module.
// Produces a shareable log file for remote debugging.
// Related: KLog.kt (API), FileLogger.kt (impl), LoggingModule.kt (Hilt)

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kriniks.kcam.core.logging"
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
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    api(libs.timber)
    implementation(libs.coroutines.android)
}
