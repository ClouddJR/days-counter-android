package com.arkadiusz.dayscounter

import android.app.Application
import android.os.Build
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.NotificationUtils
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings


/**
 * Created by Arkadiusz on 14.03.2018
 */

class DaysCounterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeRealm()
        createNotificationChannelForReminders()

        MobileAds.initialize(this, "ca-app-pub-4098342918729972~7968062049")

        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(false)
                .setPersistenceEnabled(false)
                .build()
        firestore.firestoreSettings = settings
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
