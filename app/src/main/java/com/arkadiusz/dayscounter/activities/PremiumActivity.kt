package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.utils.PurchasesUtils
import com.arkadiusz.dayscounter.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_premium.*
import org.jetbrains.anko.alert

class PremiumActivity : AppCompatActivity() {

    private lateinit var helper: IabHelper
    private var isHelperSetup = false
    private var isPremiumAlreadyBought = false
    private var isPremiumBigAlreadyBought = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        setUpHelper()
        setUpButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!::helper.isInitialized) return
        if (!helper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setUpHelper() {
        PurchasesUtils.sharedPreferences = defaultPrefs(this)
        helper = IabHelper(this, PurchasesUtils.base64EncodedPublicKey)
        helper.startSetup { result ->
            if (result.isSuccess) {
                isHelperSetup = true
                try {

                    //checking if a user have already bought any of the premium accounts
                    helper.queryInventoryAsync { res, inv ->
                        if (res?.isFailure != false) {
                            //nothing
                        } else {
                            val purchasePro = inv?.getPurchase("1")

                            if (purchasePro != null) {
                                isPremiumAlreadyBought = true
                            }
                            val purchaseProBig = inv?.getPurchase("2")

                            if (purchaseProBig != null) {
                                isPremiumBigAlreadyBought = true
                            }
                        }
                    }
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setUpButtons() {
        buyButton.setOnClickListener {
            if (isHelperSetup) {

                if (!isPremiumAlreadyBought) {
                    try {
                        helper.launchPurchaseFlow(this, "1", 10001,
                                PurchasesUtils.PurchaseFinishedListener, "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    alert(getString(R.string.premium_already_own_dialog_message)) {
                        title = getString(R.string.premium_already_own_dialog_title)
                        positiveButton("OK") {}
                    }.show()
                }
            }

        }

        buyButtonBig.setOnClickListener {
            if (isHelperSetup) {
                if (!isPremiumBigAlreadyBought) {
                    try {
                        helper.launchPurchaseFlow(this, "2", 10001,
                                PurchasesUtils.PurchaseFinishedListener, "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    alert(getString(R.string.premium_already_own_dialog_message)) {
                        title = getString(R.string.premium_already_own_dialog_title)
                        positiveButton("OK") {}
                    }.show()
                }
            }

        }
    }
}