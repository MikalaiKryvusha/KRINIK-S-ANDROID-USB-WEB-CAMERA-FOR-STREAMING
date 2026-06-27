/**
 * LoggingModule — wires FileLogger into Hilt and connects it to KLog.
 * Called once from KrinikCamApp via @HiltAndroidApp startup.
 */

package com.kriniks.kcam.core.logging.di

import android.content.Context
import com.kriniks.kcam.core.logging.FileLogger
import com.kriniks.kcam.core.logging.KLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {

    @Provides
    @Singleton
    fun provideFileLogger(@ApplicationContext context: Context): FileLogger {
        return FileLogger(context).also { KLog.fileLogger = it }
    }
}
