package com.kriniks.kcam

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application entry point.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and initialise the dependency injection graph for the whole app.
 *
 * Related modules: all feature modules depend on the DI graph rooted here.
 */
@HiltAndroidApp
class KrinikCamApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Plant debug logging tree only in debug builds to avoid log leaks in release
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
