package com.bitdrive.screenmonitor.di

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class)
object AppModule {

    @Provides
    fun provideUsageStatsManager(@ApplicationContext context: Context): UsageStatsManager {
        return context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    @Provides
    fun provideAppOpsManager(@ApplicationContext context: Context): AppOpsManager {
        return context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    }
}