package com.meshcommand.app.di

import android.content.Context
import androidx.room.Room
import com.meshcommand.app.comm.UsbSerialManager
import com.meshcommand.app.data.MeshDatabase
import com.meshcommand.app.data.dao.EventDao
import com.meshcommand.app.data.dao.PositionHistoryDao
import com.meshcommand.app.data.dao.SoldierDao
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

    @Provides
    @Singleton
    fun provideMeshDatabase(@ApplicationContext context: Context): MeshDatabase {
        return Room.databaseBuilder(
            context,
            MeshDatabase::class.java,
            "mesh_command.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSoldierDao(database: MeshDatabase): SoldierDao {
        return database.soldierDao()
    }

    @Provides
    fun provideEventDao(database: MeshDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    fun providePositionHistoryDao(database: MeshDatabase): PositionHistoryDao {
        return database.positionHistoryDao()
    }
}
