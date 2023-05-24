package com.bitdrive.screenmonitor.services

import android.app.Notification.CATEGORY_STATUS
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bitdrive.screenmonitor.utils.NotificationCentral
import com.bitdrive.screenmonitor.R
import com.bitdrive.screenmonitor.main.MainActivity

class TimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(
            this,
            NotificationCentral.Channel.TIMER.id
        )
        val notification = builder
            .setOngoing(true)
            .setContentTitle("Timer")
            .setContentText("This notification comer from the TimerService")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCategory(CATEGORY_STATUS)
            .build()
        startForeground(777, notification)
        return START_REDELIVER_INTENT
    }
}