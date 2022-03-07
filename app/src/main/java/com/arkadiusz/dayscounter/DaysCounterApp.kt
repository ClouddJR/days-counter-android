package com.arkadiusz.dayscounter

import android.app.Application
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.arkadiusz.dayscounter.data.worker.WidgetUpdateWorker
import com.arkadiusz.dayscounter.util.NotificationUtils
import com.arkadiusz.dayscounter.util.PreferenceUtils
import com.arkadiusz.dayscounter.util.PurchasesUtils
import com.arkadiusz.dayscounter.util.billing.BillingDataSource
import com.arkadiusz.dayscounter.util.billing.BillingRepository
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DaysCounterApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    lateinit var billingRepository: BillingRepository

    override fun onCreate() {
        super.onCreate()
        initializeBilling()
        initializeRealm()
        initializeAds()
        createNotificationChannelForReminders()
        enqueueWorkerUpdatingWidgets()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeBilling() {
        val billingDataSource = BillingDataSource.getInstance(
            this,
            GlobalScope,
            arrayOf(PurchasesUtils.PREMIUM_SKU, PurchasesUtils.PREMIUM_BIG_SKU),
            null,
            null
        )
        billingRepository = BillingRepository(
            GlobalScope,
            billingDataSource,
            PreferenceUtils.defaultPrefs(this)
        )
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

    private fun enqueueWorkerUpdatingWidgets() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WidgetUpdateWorker.PERIODIC_WORK_WIDGET_UPDATE,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(3, TimeUnit.HOURS).build()
        )
    }
}
