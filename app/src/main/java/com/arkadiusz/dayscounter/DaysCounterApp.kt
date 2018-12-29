package com.arkadiusz.dayscounter

import android.os.Build
import androidx.multidex.MultiDexApplication
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.NotificationUtils
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DaysCounterApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        initializeRealm()
        initializeFirebaseOfflinePersistence()
        createNotificationChannelForReminders()
    }

    private fun initializeRealm() {
        DatabaseRepository.RealmInitializer.initRealm(this)
    }

    private fun initializeFirebaseOfflinePersistence() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    private fun createNotificationChannelForReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(applicationContext)
        }
    }
}
