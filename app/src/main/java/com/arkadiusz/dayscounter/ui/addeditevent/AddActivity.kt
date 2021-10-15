package com.arkadiusz.dayscounter.ui.addeditevent

import android.os.Bundle
import android.widget.TextView
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.util.DateUtils.formatDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.util.DateUtils.generateTodayCalendar
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.PreferenceUtils.set
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.android.synthetic.main.content_add.*
import java.util.*

class AddActivity : BaseAddEditActivity() {

    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCurrentDateInForm()
        setUpAd()
    }

    override fun handleSaveClick() {
        showAd()
        prepareEventBasedOnViews().also { event ->
            viewModel.addEvent(event)
            addReminder(event)
        }
        finish()
    }

    private fun setCurrentDateInForm() {
        val calendar = generateTodayCalendar()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        chosenYear = year
        chosenMonth = month
        chosenDay = day

        date = formatDate(year, month, day)

        dateEditText.setText(
            formatDateAccordingToSettings(
                date,
                defaultPrefs(this)["dateFormat"] ?: ""
            )
        )

        eventCalculateText.text = generateCounterText()
    }

    private fun setUpAd() {
        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean = prefs["ads", false] ?: false
        val wasShown: Boolean = prefs["wasAdShown", true] ?: true

        val adUnitId = "ca-app-pub-4098342918729972/3144606816"
        if (!areAdsRemoved && !wasShown) {
            InterstitialAd.load(
                this,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }
                }
            )
        }
    }

    private fun showAd() {
        interstitialAd?.show(this)
        defaultPrefs(this)["wasAdShown"] = interstitialAd != null
    }

    private fun prepareEventBasedOnViews(): Event {
        val event = Event()
        event.name = titleEditText.text.toString()
        event.date = date
        event.description = descriptionEditText.text.toString()
        event.image = imageUri.toString()
        event.imageID = imageID
        event.imageColor = imageColor
        event.type = intent.getStringExtra("Event Type")!!
        event.repeat = repeatSpinner.selectedItemPosition.toString()
        if (hasAlarm) {
            event.hasAlarm = true
            event.reminderYear = chosenReminderYear
            event.reminderMonth = chosenReminderMonth
            event.reminderDay = chosenReminderDay
            event.reminderHour = chosenReminderHour
            event.reminderMinute = chosenReminderMinute
            event.notificationText = reminderTextEditText.text.toString()
        } else {
            event.hasAlarm = false
        }

        event.formatYearsSelected = yearsCheckbox.isChecked
        event.formatMonthsSelected = monthsCheckbox.isChecked
        event.formatWeeksSelected = weeksCheckbox.isChecked
        event.formatDaysSelected = daysCheckbox.isChecked
        event.lineDividerSelected = showDividerCheckbox.isChecked
        event.counterFontSize =
            (counterFontSizeSpinner.getChildAt(0) as TextView).text.toString().toInt()
        event.titleFontSize =
            (titleFontSizeSpinner.getChildAt(0) as TextView).text.toString().toInt()
        event.fontType = (fontTypeSpinner.getChildAt(0) as TextView).text.toString()
        event.fontColor = selectedColor
        event.pictureDim = dimValue
        return event
    }
}