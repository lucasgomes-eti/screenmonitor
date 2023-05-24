package com.bitdrive.screenmonitor.di

import android.app.Application
import androidx.room.Room
import com.bitdrive.screenmonitor.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(app: Application): Database {
        return Room.databaseBuilder(app, Database::class.java, "screenmonitor.db")
            .build()
    }
}