package com.kriniks.kcam.feature.codec.di

import com.kriniks.kcam.feature.codec.CodecScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CodecModule {

    @Provides
    @Singleton
    fun provideCodecScanner(): CodecScanner = CodecScanner()
}
