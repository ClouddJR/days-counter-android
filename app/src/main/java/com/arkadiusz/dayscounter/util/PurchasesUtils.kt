package com.arkadiusz.dayscounter.util

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.PreferenceUtils.set
import android.content.Context
import android.content.SharedPreferences
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.premium.PremiumActivity
import com.arkadiusz.dayscounter.util.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.util.purchaseutils.IabResult
import com.arkadiusz.dayscounter.util.purchaseutils.Inventory
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

object PurchasesUtils {

    const val base64EncodedPublicKey =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAljgvqjNFwvk8KX5N1yfCAb+dOtaN5vYiERZ4JwpJfQKV2IQEQ04H9+mZE6FyoH6g5LyFAuFY28eJCoNWNsQ4rjDgU33Ta4ZXjxdLCPyw5rwkWU8LoJIxjaf9Ftau62d2SvkcDFDFSV70RUyU6UxlDeDXblZgD799A1zwMPCXLVeKqTnK7GqXsGo48KfBsbMgKsn7gKWuTNSO0RK3UTH8TkzKkjFF97QSBRN6WLWcTHNWzttb+BIMZWZv5H6TIySo/d5MKwKPPojLRDRpepZsjGrGD9td93SN+4X/kFW9t0/S+3Gl1tQWnzDCVhLFhK7aR0hDqFYPMLQGDqcHNZSpbwIDAQAB"
    lateinit var sharedPreferences: SharedPreferences

    object GotInventoryListener : IabHelper.QueryInventoryFinishedListener {
        override fun onQueryInventoryFinished(result: IabResult?, inv: Inventory?) {
            if (result?.isFailure != false) {
                //nothing
            } else {
                val purchasePro = inv?.getPurchase("1")
                if (purchasePro != null) {
                    sharedPreferences["ads"] = true
                }

                val purchaseProBig = inv?.getPurchase("2")
                if (purchaseProBig != null) {
                    sharedPreferences["ads"] = true
                }
            }
        }
    }

    fun displayPremiumInfoDialog(context: Context?) {
        context?.alert(
            context.getString(R.string.premium_dialog_content),
            context.getString(R.string.premium_dialog_title)
        ) {
            positiveButton(context.getString(R.string.premium_dialog_more_button)) {
                context.startActivity<PremiumActivity>()
                it.dismiss()
            }
            negativeButton(context.getString(R.string.add_activity_back_button_cancel)) {
                it.dismiss()
            }
        }?.show()
    }

    fun isPremiumUser(context: Context?): Boolean {
        context?.let {
            val prefs = defaultPrefs(context)
            return prefs["ads"] ?: false
        }

        return false
    }
}