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

class PremiumActivity : AppCompatActivity() {

    private lateinit var helper: IabHelper
    private var isHelperSetup = false

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
        helper = IabHelper(this, PurchasesUtils.base64EncodedPublicKey)
        helper.startSetup { result ->
            if (result.isSuccess) {
                isHelperSetup = true
            }
        }
        PurchasesUtils.sharedPreferences = defaultPrefs(this)
    }

    private fun setUpButtons() {
        buyButton.setOnClickListener {
            if (isHelperSetup) {
                try {
                    helper.launchPurchaseFlow(this, "1", 10001,
                            PurchasesUtils.PurchaseFinishedListener, "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        buyButtonBig.setOnClickListener {
            if (isHelperSetup) {
                try {
                    helper.launchPurchaseFlow(this, "2", 10001,
                            PurchasesUtils.PurchaseFinishedListener, "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }
}