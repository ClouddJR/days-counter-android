package com.arkadiusz.dayscounter

import android.app.Application
import android.os.Build
import com.arkadiusz.dayscounter.util.NotificationUtils
import com.google.android.gms.ads.MobileAds
import io.realm.Realm

class DaysCounterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeRealm()
        initializeAds()
        createNotificationChannelForReminders()
    }

    private fun initializeRealm() {
        Realm.init(this)
    }

    private fun initializeAds() {
        MobileAds.initialize(this) { }
    }

    private fun createNotificationChannelForReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(applicationContext)
        }
    }
}
