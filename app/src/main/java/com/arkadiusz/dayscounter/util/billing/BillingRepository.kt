package com.arkadiusz.dayscounter.util.billing

import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleObserver
import com.arkadiusz.dayscounter.util.PreferenceUtils.set
import com.arkadiusz.dayscounter.util.PurchasesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BillingRepository(
    defaultScope: CoroutineScope,
    private val billingDataSource: BillingDataSource,
    private val sharedPreferences: SharedPreferences,
) {
    init {
        defaultScope.launch {
            val isPremiumBought = billingDataSource.isPurchased(PurchasesUtils.PREMIUM_SKU)
            val isPremiumBigBought = billingDataSource.isPurchased(PurchasesUtils.PREMIUM_BIG_SKU)

            isPremiumBought.combine(isPremiumBigBought) { premiumBought, premiumBigBought ->
                premiumBought || premiumBigBought
            }
                .collect { isAnyPremiumBought ->
                    if (isAnyPremiumBought) {
                        acknowledgePremium()
                    }
                }
        }
    }

    fun getBillingLifecycleObserver(): LifecycleObserver {
        return billingDataSource
    }

    fun getNewPurchases(): SharedFlow<List<String>> {
        return billingDataSource.getNewPurchases()
    }

    fun canBuy(sku: String): Flow<Boolean> {
        return billingDataSource.canPurchase(sku)
    }

    fun isBought(sku: String): Flow<Boolean> {
        return billingDataSource.isPurchased(sku)
    }

    fun getSkuPrice(sku: String): Flow<String> {
        return billingDataSource.getSkuPrice(sku)
    }

    fun buy(activity: Activity?, sku: String) {
        billingDataSource.launchBillingFlow(activity, sku)
    }

    private fun acknowledgePremium() {
        sharedPreferences["ads"] = true
    }
}