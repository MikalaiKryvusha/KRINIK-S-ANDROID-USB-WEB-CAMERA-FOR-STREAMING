// :feature:usb — UVC camera detection, USB permission flow, preview, hot-plug.
// Wraps AndroidUSBCamera (jiangdongguo). Exposes Flow<UsbEvent> to other modules.
// Related: UvcCameraManager, UsbDeviceRepository, UvcPreviewView (Compose)

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kriniks.kcam.feature.usb"
    compileSdk = 35

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:logging"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coroutines.android)

    // UVC camera driver. Uses `api` because MultiCameraClient.Camera appears in UsbUiState
    // (public API consumed by :app), so the type must be visible to consumers.
    // Exclude UI-only transitive deps that are only needed by libausbc's demo screen.
    api(libs.android.usb.camera) {
        exclude(group = "com.gyf.immersionbar")   // status-bar styling, demo only
        exclude(group = "com.zlc.glide")          // webp decoder, private JitPack artifact
        exclude(group = "com.github.bumptech.glide") // image loading, not needed for UVC
    }
    // libuvc contains com.serenegiant.usb.USBMonitor referenced in IDeviceConnectCallBack.
    // The libausbc POM lists it as `runtime` scope only, so it's not on the compile classpath.
    // compileOnly is enough because libuvc IS on the runtime classpath via the transitive dep.
    compileOnly(libs.android.usb.camera.libuvc)
}
