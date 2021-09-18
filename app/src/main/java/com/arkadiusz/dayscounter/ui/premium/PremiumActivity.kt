package com.arkadiusz.dayscounter.ui.premium

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.set
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.util.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.util.purchaseutils.IabResult
import com.arkadiusz.dayscounter.util.purchaseutils.Purchase
import com.arkadiusz.dayscounter.util.PurchasesUtils
import com.arkadiusz.dayscounter.util.ThemeUtils
import kotlinx.android.synthetic.main.activity_premium.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast

class PremiumActivity : AppCompatActivity(), IabHelper.OnIabPurchaseFinishedListener {

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
                        helper.launchPurchaseFlow(
                            this, "1", 10001,
                            this, ""
                        )
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
                        helper.launchPurchaseFlow(
                            this, "2", 10001,
                            this, ""
                        )
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

    override fun onIabPurchaseFinished(result: IabResult?, purchase: Purchase?) {
        result?.let {
            when {
                result.isFailure -> {
                    //nothing
                }
                purchase?.sku == "1" -> {
                    PurchasesUtils.sharedPreferences["ads"] = true
                    displayKonfetti()
                }
                purchase?.sku == "2" -> {
                    PurchasesUtils.sharedPreferences["ads"] = true
                    displayKonfetti()
                }
            }
        }
    }

    private fun displayKonfetti() {
        viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.RECT, Shape.CIRCLE)
            .addSizes(Size(12))
            .setPosition(-50f, viewKonfetti.width + 50f, -50f, -50f)
            .streamFor(200, 3000L)
        longToast(getString(R.string.premium_thank_you_dialog))
    }
}