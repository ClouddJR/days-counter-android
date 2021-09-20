package com.arkadiusz.dayscounter.ui.premium

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.arkadiusz.dayscounter.util.PurchasesUtils
import com.arkadiusz.dayscounter.util.billing.BillingRepository
import kotlinx.coroutines.flow.combine

class PremiumActivityViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    fun isAnyPremiumBought(): LiveData<Boolean> {
        val isPremiumBought = billingRepository.isBought(PurchasesUtils.PREMIUM_SKU)
        val isPremiumBigBought = billingRepository.isBought(PurchasesUtils.PREMIUM_BIG_SKU)

        return isPremiumBought.combine(isPremiumBigBought) { premiumBought, premiumBigBought ->
            premiumBought || premiumBigBought
        }.asLiveData()
    }

    fun canBuyPremium(): LiveData<Boolean> {
        return billingRepository.canBuy(PurchasesUtils.PREMIUM_SKU).asLiveData()
    }

    fun canBuyPremiumBig(): LiveData<Boolean> {
        return billingRepository.canBuy(PurchasesUtils.PREMIUM_BIG_SKU).asLiveData()
    }

    fun premiumPrice(): LiveData<String> {
        return billingRepository.getSkuPrice(PurchasesUtils.PREMIUM_SKU).asLiveData()
    }

    fun premiumBigPrice(): LiveData<String> {
        return billingRepository.getSkuPrice(PurchasesUtils.PREMIUM_BIG_SKU).asLiveData()
    }

    fun buyPremium(activity: Activity?) {
        billingRepository.buy(activity, PurchasesUtils.PREMIUM_SKU)
    }

    fun buyPremiumBig(activity: Activity?) {
        billingRepository.buy(activity, PurchasesUtils.PREMIUM_BIG_SKU)
    }

    fun newPurchases(): LiveData<List<String>> {
        return billingRepository.getNewPurchases().asLiveData()
    }
}