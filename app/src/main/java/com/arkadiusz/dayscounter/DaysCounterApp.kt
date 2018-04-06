package com.arkadiusz.dayscounter

import android.os.Build
import android.support.multidex.MultiDexApplication
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.NotificationUtils

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DaysCounterApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        initializeRealm()
        createNotificationChannelForReminders()
    }

    private fun initializeRealm() {
        DatabaseRepository.RealmInitializer.initRealm(this)
    }

    private fun createNotificationChannelForReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(applicationContext)
        }
    }
}
