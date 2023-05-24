package com.bitdrive.screenmonitor.main

import android.app.Application
import android.app.NotificationManager
import com.bitdrive.screenmonitor.utils.NotificationCentral
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScreenMonitor : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationCentral.createChannel(applicationContext, NotificationCentral.Channel.TIMER, NotificationManager.IMPORTANCE_LOW)
    }
}