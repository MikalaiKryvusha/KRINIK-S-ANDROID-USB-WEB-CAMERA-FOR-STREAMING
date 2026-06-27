package com.kriniks.kcam.feature.capture.di

import com.kriniks.kcam.feature.capture.DeviceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CaptureModule {

    @Provides
    @Singleton
    fun provideDeviceManager(): DeviceManager = DeviceManager()
}
