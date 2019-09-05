package com.arkadiusz.dayscounter.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arkadiusz.dayscounter.data.local.DatabaseProvider
import com.arkadiusz.dayscounter.utils.RemindersUtils

/**
 * Created by arkadiusz on 28.03.18
 */

class AlarmBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val results = DatabaseProvider.provideRepository().getEventsWithAlarms()

            for (event in results) {
                RemindersUtils.addNewReminder(context, event)
            }
        }
    }
}