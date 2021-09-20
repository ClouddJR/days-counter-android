package com.arkadiusz.dayscounter.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.utils.NotificationUtils

/**
 * Created by arkadiusz on 28.03.18
 */

class AlarmBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getStringExtra("eventId")
        val eventTitle = intent?.getStringExtra("eventTitle")
        val eventDescription = intent?.getStringExtra("eventText")
        val eventDate = intent?.getStringExtra("eventDate")

        if (id != null && eventTitle != null && eventDate != null && eventDescription != null) {
            NotificationUtils.createNotification(context, eventTitle, eventDescription, id)
            val databaseRepository = DatabaseRepository()
            databaseRepository.disableAlarmForEvent(id)
            databaseRepository.closeDatabase()
        }
    }
}