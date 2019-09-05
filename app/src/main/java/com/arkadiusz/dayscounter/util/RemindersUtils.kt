package com.arkadiusz.dayscounter.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.receiver.AlarmBroadcast
import org.jetbrains.anko.alarmManager
import java.util.*

/**
 * Created by arkadiusz on 04.03.18
 */

object RemindersUtils {

    fun addNewReminder(context: Context, event: Event) {
        val alarmManager = context.alarmManager
        val calendar = generateCalendar(event)
        val alarmIntent = buildPendingIntent(event, context)

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
    }

    fun deleteReminder(context: Context, event: Event) {
        val alarmManager = context.alarmManager
        val alarmIntent = buildPendingIntent(event, context)

        alarmManager.cancel(alarmIntent)
    }


    private fun generateCalendar(event: Event): Calendar {
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.clear()
        c.set(event.reminderYear, event.reminderMonth, event.reminderDay, event.reminderHour, event.reminderMinute)
        return c
    }

    private fun buildPendingIntent(event: Event, context: Context): PendingIntent {
        val intentSend = Intent(context, AlarmBroadcast::class.java)
        intentSend.putExtra("eventTitle", event.name)
        intentSend.putExtra("eventText", event.notificationText)
        intentSend.putExtra("eventId", event.id)
        intentSend.putExtra("eventDate", event.date)

        return PendingIntent.getBroadcast(context, event.id.hashCode(), intentSend, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}