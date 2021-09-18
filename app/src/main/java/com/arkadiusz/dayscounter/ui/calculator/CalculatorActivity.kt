package com.arkadiusz.dayscounter.ui.calculator

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.DateComponents
import com.arkadiusz.dayscounter.util.ExtensionUtils.getViewModel
import com.arkadiusz.dayscounter.util.DateUtils.generateCounterText
import com.arkadiusz.dayscounter.util.PurchasesUtils
import com.arkadiusz.dayscounter.util.ThemeUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.android.synthetic.main.activity_calculator.*
import kotlinx.android.synthetic.main.activity_calculator.view.*
import kotlinx.android.synthetic.main.ad_layout.*
import kotlinx.android.synthetic.main.event_compact_counter_stack.view.*
import java.util.*

class CalculatorActivity : AppCompatActivity() {

    private lateinit var viewModel: CalculatorViewModel
    private lateinit var adLoader: AdLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)
        title = ""
        initViewModel()
        initOnClickListeners()
        observeViewModelUpdates()
        initAdView()
        displayAd()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calculator, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_calculate -> {
                viewModel.calculate(
                    CalculatorComponentsHolder(
                        daysCheckbox.isChecked,
                        monthsCheckbox.isChecked,
                        weeksCheckbox.isChecked,
                        yearsCheckbox.isChecked
                    )
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initViewModel() {
        viewModel = getViewModel(this) { CalculatorViewModel(defaultPrefs(this)) }
    }

    private fun initOnClickListeners() {
        startDateEditText.setOnClickListener { viewModel.startDateEditTextClicked() }
        endDateEditText.setOnClickListener { viewModel.endDateEditTextClicked() }
    }

    private fun observeViewModelUpdates() {
        viewModel.calculatedComponents.observe(this, Observer { components ->
            showResultCardAndHideCounterSections()
            showAndFillRelevantSections(components)
        })

        viewModel.additionalFormatsCalculatedComponents.observe(
            this,
            Observer { additionalFormats ->
                removePreviousAdditionalFormats()
                addAdditionalFormats(additionalFormats)
                scrollToBottom()
            })

        viewModel.showStartDatePicker.observe(this, Observer { previouslyChosenComponents ->
            showDatePicker(startDateEditText, previouslyChosenComponents)
        })

        viewModel.showEndDatePicker.observe(this, Observer { previouslyChosenComponents ->
            showDatePicker(endDateEditText, previouslyChosenComponents)
        })

        viewModel.chosenStartDate.observe(this, Observer { date ->
            startDateEditText.setText(date)
            startDateEditText.error = null
        })

        viewModel.chosenEndDate.observe(this, Observer { date ->
            endDateEditText.setText(date)
            endDateEditText.error = null
        })

        viewModel.formNotValid.observe(this, Observer {
            if (startDateEditText.text?.isEmpty() == true) {
                startDateEditText.error = "This field is required"
            }
            if (endDateEditText.text?.isEmpty() == true) {
                endDateEditText.error = "This field is required"
            }
        })
    }

    private fun showResultCardAndHideCounterSections() {
        resultCardView.visibility = View.VISIBLE
        resultCardView.counterStackView.yearsSection.visibility = View.GONE
        resultCardView.counterStackView.monthsSection.visibility = View.GONE
        resultCardView.counterStackView.weeksSection.visibility = View.GONE
        resultCardView.counterStackView.daysSection.visibility = View.GONE
    }

    private fun showAndFillRelevantSections(components: DateComponents) {
        if (yearsCheckbox.isChecked) {
            resultCardView.counterStackView.yearsSection.visibility = View.VISIBLE
            resultCardView.counterStackView.yearsCaptionTextView.text = resources.getQuantityText(
                R.plurals.years_number, components.years
            )
            resultCardView.counterStackView.yearsNumberTextView
                .text = components.years.toString()
        }

        if (monthsCheckbox.isChecked) {
            resultCardView.counterStackView.monthsSection.visibility = View.VISIBLE
            resultCardView.counterStackView.monthsCaptionTextView.text = resources.getQuantityText(
                R.plurals.months_number, components.months
            )
            resultCardView.counterStackView.monthsNumberTextView
                .text = components.months.toString()
        }

        if (weeksCheckbox.isChecked) {
            resultCardView.counterStackView.weeksSection.visibility = View.VISIBLE
            resultCardView.counterStackView.weeksCaptionTextView.text = resources.getQuantityText(
                R.plurals.weeks_number, components.weeks
            )
            resultCardView.counterStackView.weeksNumberTextView
                .text = components.weeks.toString()
        }

        if (daysCheckbox.isChecked) {
            resultCardView.counterStackView.daysSection.visibility = View.VISIBLE
            resultCardView.counterStackView.daysCaptionTextView.text = resources.getQuantityText(
                R.plurals.days_number, components.days
            )
            resultCardView.counterStackView.daysNumberTextView
                .text = components.days.toString()
        }
    }

    private fun removePreviousAdditionalFormats() {
        resultLinearLayout.removeViews(3, resultLinearLayout.childCount - 3)
    }

    private fun addAdditionalFormats(
        additionalFormats: List<Pair<CalculatorComponentsHolder,
                DateComponents>>
    ) {
        additionalFormats.forEach { pair ->
            val componentsHolder = pair.first
            val calculatedComponents = pair.second
            val counterText = createCounterTextForAdditionalFormat(
                componentsHolder,
                calculatedComponents
            )
            val textView = createAdditionalFormatTextView(counterText)
            resultLinearLayout.addView(textView)
        }
    }

    private fun createCounterTextForAdditionalFormat(
        componentsHolder: CalculatorComponentsHolder,
        calculatedComponents: DateComponents
    ): String {
        return if (!componentsHolder.onlyWorkDays) {
            generateCounterText(
                calculatedComponents.years,
                calculatedComponents.months, calculatedComponents.weeks,
                calculatedComponents.days, componentsHolder.areYearsIncluded,
                componentsHolder.areMonthsIncluded, componentsHolder.areWeeksIncluded,
                componentsHolder.areDaysIncluded, this
            )
        } else {
            "${calculatedComponents.days} " +
                    resources.getQuantityString(
                        R.plurals.workdays_number, calculatedComponents.days
                    )
        }
    }

    private fun createAdditionalFormatTextView(counterText: String): TextView {
        val textView = TextView(this)
        val params = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        params.marginStart = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f, resources.displayMetrics
        ).toInt()
        textView.layoutParams = params
        textView.text = counterText
        return textView
    }

    private fun scrollToBottom() {
        parentScrollView.post {
            parentScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun showDatePicker(editText: EditText, components: DateComponents) {
        val calendar = Calendar.getInstance()
        val year = if (components.years == 0) calendar.get(Calendar.YEAR) else components.years
        val month = if (components.years == 0) calendar.get(Calendar.MONTH) else components.months
        val day =
            if (components.years == 0) calendar.get(Calendar.DAY_OF_MONTH) else components.days

        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, chosenYear,
                                                                    chosenMonth, chosenDay ->
            val chosenDateComponents = DateComponents(
                years = chosenYear,
                months = chosenMonth,
                days = chosenDay
            )

            if (editText == startDateEditText) {
                viewModel.dateForStartDateChosen(chosenDateComponents)
            } else {
                viewModel.dateForEndDateChosen(chosenDateComponents)
            }

        }, year, month, day).show()
    }

    private fun initAdView() {
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.priceView = adView.findViewById(R.id.adPrice)
        adView.starRatingView = adView.findViewById(R.id.adStars)
        adView.storeView = adView.findViewById(R.id.adStore)
        adView.advertiserView = adView.findViewById(R.id.adAdvertiser)
        adView.mediaView = adView.findViewById(R.id.ad_media)
    }

    private fun displayAd() {
        if (!PurchasesUtils.isPremiumUser(this)) {
            try {
                val builder = AdLoader.Builder(this, "ca-app-pub-4098342918729972/9751345592")
                adLoader = builder.forNativeAd { nativeAd ->
                    //ad loaded successfully
                    if (!adLoader.isLoading) {
                        if (adCardView != null) {
                            adCardView.visibility = View.VISIBLE
                            populateNativeAdView(nativeAd)
                        }
                    }
                }.withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            //ad failed to load, so hide ad section
                            if (adCardView != null) {
                                adCardView.visibility = View.GONE
                            }
                        }
                    }).build()

                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd) {
        // These assets are guaranteed to be in every UnifiedNativeAd
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction


        // These assets aren't guaranteed to be in every UnifiedNativeAd
        val icon = nativeAd.icon

        if (icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }
}