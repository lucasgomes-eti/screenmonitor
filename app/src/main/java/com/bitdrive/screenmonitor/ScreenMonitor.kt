package com.bitdrive.screenmonitor

import android.app.Application
import android.app.NotificationManager

class ScreenMonitor : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationCentral.createChannel(applicationContext, NotificationCentral.Channel.TIMER, NotificationManager.IMPORTANCE_LOW)
    }
}