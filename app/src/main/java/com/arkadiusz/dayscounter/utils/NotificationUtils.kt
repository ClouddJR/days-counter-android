package com.arkadiusz.dayscounter.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.activities.DetailActivity
import org.jetbrains.anko.notificationManager

/**
 * Created by arkadiusz on 28.03.18
 */

object NotificationUtils {

    private const val channelID = "channel_1"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context?) {
        val notificationManager = context?.notificationManager
        val name = context?.getString(R.string.notification_channel_name)
        val description = context?.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.YELLOW
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)

        notificationManager?.createNotificationChannel(channel)
    }

    fun createNotification(context: Context?, eventTitle: String, eventDescription: String, eventId: String) {
        val mBuilder = NotificationCompat.Builder(context!!, channelID)
                .setSmallIcon(R.drawable.n_icon)
                .setContentTitle(eventTitle)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentText(eventDescription)
        val pendingIntent = buildPendingIntent(context, eventId)
        mBuilder.setContentIntent(pendingIntent)
        val notificationManager = context.notificationManager
        notificationManager.notify(eventId.hashCode(), mBuilder.build())
    }

    private fun buildPendingIntent(context: Context?, eventId: String): PendingIntent {
        val resultIntent = Intent(context, DetailActivity::class.java)
        resultIntent.putExtra("event_id", eventId)
        resultIntent.putExtra("notificationClick", "clicked")

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(resultIntent)

        return stackBuilder.getPendingIntent(eventId.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT)
    }
}