package com.arkadiusz.dayscounter.utils

import PreferenceUtils.set
import android.content.SharedPreferences
import com.arkadiusz.dayscounter.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.purchaseutils.IabResult
import com.arkadiusz.dayscounter.purchaseutils.Inventory
import com.arkadiusz.dayscounter.purchaseutils.Purchase

object PurchasesUtils {

    const val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAljgvqjNFwvk8KX5N1yfCAb+dOtaN5vYiERZ4JwpJfQKV2IQEQ04H9+mZE6FyoH6g5LyFAuFY28eJCoNWNsQ4rjDgU33Ta4ZXjxdLCPyw5rwkWU8LoJIxjaf9Ftau62d2SvkcDFDFSV70RUyU6UxlDeDXblZgD799A1zwMPCXLVeKqTnK7GqXsGo48KfBsbMgKsn7gKWuTNSO0RK3UTH8TkzKkjFF97QSBRN6WLWcTHNWzttb+BIMZWZv5H6TIySo/d5MKwKPPojLRDRpepZsjGrGD9td93SN+4X/kFW9t0/S+3Gl1tQWnzDCVhLFhK7aR0hDqFYPMLQGDqcHNZSpbwIDAQAB"
    lateinit var sharedPreferences: SharedPreferences

    object GotInventoryListener : IabHelper.QueryInventoryFinishedListener {
        override fun onQueryInventoryFinished(result: IabResult, inv: Inventory) {
            if (result.isFailure) {
                //nothing
            } else {
                val purchase = inv.getPurchase("1")
                if (purchase != null) {
                    sharedPreferences["ads"] = true
                }
            }
        }
    }

    object PurchaseFinishedListener : IabHelper.OnIabPurchaseFinishedListener {
        override fun onIabPurchaseFinished(result: IabResult, purchase: Purchase) {
            if (result.isFailure) {
                //nothing
            } else if (purchase.sku == "1") {
                sharedPreferences["ads"] = true
            }
        }
    }
}