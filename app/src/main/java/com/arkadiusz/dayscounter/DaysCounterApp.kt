package com.arkadiusz.dayscounter

import android.app.Application
import android.os.Build
import com.arkadiusz.dayscounter.utils.NotificationUtils
import com.google.android.gms.ads.MobileAds
import io.realm.Realm


/**
 * Created by Arkadiusz on 14.03.2018
 */

class DaysCounterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeRealm()
        createNotificationChannelForReminders()

        MobileAds.initialize(this, "ca-app-pub-4098342918729972~7968062049")
    }

    private fun initializeRealm() {
        Realm.init(this)
    }

    private fun createNotificationChannelForReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(applicationContext)
        }
    }
}
