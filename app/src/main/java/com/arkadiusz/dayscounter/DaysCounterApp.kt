package com.arkadiusz.dayscounter

import android.app.Application
import android.os.Build
import com.arkadiusz.dayscounter.util.NotificationUtils
import com.arkadiusz.dayscounter.util.PreferenceUtils
import com.arkadiusz.dayscounter.util.PurchasesUtils
import com.arkadiusz.dayscounter.util.billing.BillingDataSource
import com.arkadiusz.dayscounter.util.billing.BillingRepository
import com.google.android.gms.ads.MobileAds
import io.realm.Realm
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

class DaysCounterApp : Application() {

    lateinit var billingRepository: BillingRepository

    override fun onCreate() {
        super.onCreate()
        initializeBilling()
        initializeRealm()
        initializeAds()
        createNotificationChannelForReminders()
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
}
