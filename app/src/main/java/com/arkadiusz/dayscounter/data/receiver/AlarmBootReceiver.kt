package com.arkadiusz.dayscounter.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.util.RemindersUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val results = databaseRepository.getEventsWithAlarms()

            for (event in results) {
                RemindersUtils.addNewReminder(context, event)
            }
            databaseRepository.closeDatabase()
        }
    }
}