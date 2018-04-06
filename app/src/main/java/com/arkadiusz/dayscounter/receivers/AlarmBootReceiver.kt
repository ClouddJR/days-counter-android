package com.arkadiusz.dayscounter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.RemindersUtils

/**
 * Created by arkadiusz on 28.03.18
 */

class AlarmBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val results = DatabaseRepository().getEventsWithAlarms()

            for (event in results) {
                RemindersUtils.addNewReminder(context, event)
            }
        }
    }
}