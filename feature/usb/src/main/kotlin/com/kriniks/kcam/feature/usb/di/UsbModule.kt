package com.kriniks.kcam.feature.usb.di

import com.kriniks.kcam.feature.usb.data.UsbDeviceRepositoryImpl
import com.kriniks.kcam.feature.usb.domain.UsbDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UsbModule {

    @Binds
    @Singleton
    abstract fun bindUsbDeviceRepository(impl: UsbDeviceRepositoryImpl): UsbDeviceRepository
}
