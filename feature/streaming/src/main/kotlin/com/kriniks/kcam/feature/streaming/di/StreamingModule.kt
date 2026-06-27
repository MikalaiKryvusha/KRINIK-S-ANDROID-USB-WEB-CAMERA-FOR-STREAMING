package com.kriniks.kcam.feature.streaming.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// RtmpStreamer and StreamingRepository are @Singleton @Inject constructor — no manual binding needed.
@Module
@InstallIn(SingletonComponent::class)
object StreamingModule
