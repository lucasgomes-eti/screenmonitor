package com.bitdrive.screenmonitor.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bitdrive.screenmonitor.R

object NotificationCentral {

    fun createChannel(
        context: Context,
        channel: Channel,
        importanceLevel: Int = NotificationManager.IMPORTANCE_HIGH
    ) {
        val notificationChannel = NotificationChannel(
            channel.id,
            context.getString(channel.channelName),
            importanceLevel
        ).apply {
            description = context.getString(channel.channelDescription)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun notify(context: Context, channel: Channel, id: Int, title: String, content: String) {
        val builder = NotificationCompat.Builder(context, channel.id)
        val notification = builder.setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    enum class Channel(
        val id: String,
        @StringRes val channelName: Int,
        @StringRes val channelDescription: Int
        ) {
        TIMER("timer", R.string.timer, R.string.display_screen_time)
    }
}