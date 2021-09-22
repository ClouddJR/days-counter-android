package com.arkadiusz.dayscounter.ui.premium

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.DaysCounterApp
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.util.ViewModelUtils.getViewModel
import com.arkadiusz.dayscounter.util.ThemeUtils
import kotlinx.android.synthetic.main.activity_premium.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class PremiumActivity : AppCompatActivity() {

    private lateinit var viewModel: PremiumActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        initViewModel()
        setUpButtons()
    }

    private fun initViewModel() {
        viewModel = getViewModel(this) {
            PremiumActivityViewModel(
                (application as DaysCounterApp).billingRepository
            )
        }

        viewModel.canBuyPremium().observe(this) { canBuy ->
            buyButton.isEnabled = canBuy
        }

        viewModel.canBuyPremiumBig().observe(this) { canBuy ->
            buyButtonBig.isEnabled = canBuy
        }

        viewModel.premiumPrice().observe(this) { price ->
            buyButton.text = price
        }

        viewModel.premiumBigPrice().observe(this) { price ->
            buyButtonBig.text = price
        }

        viewModel.newPurchases().observe(this) {
            displayKonfetti()
        }

        viewModel.isAnyPremiumBought().observe(this) { isBought ->
            premiumBoughtTextView.visibility = if (isBought) View.VISIBLE else View.GONE
        }
    }

    private fun setUpButtons() {
        buyButton.setOnClickListener {
            viewModel.buyPremium(this)
        }

        buyButtonBig.setOnClickListener {
            viewModel.buyPremiumBig(this)
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
    }
}