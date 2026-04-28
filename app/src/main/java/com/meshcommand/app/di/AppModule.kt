package com.meshcommand.app.di

import android.content.Context
import com.meshcommand.app.comm.UsbSerialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUsbSerialManager(@ApplicationContext context: Context): UsbSerialManager {
        return UsbSerialManager(context)
    }
}
