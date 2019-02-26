package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.repositories.DatabaseProvider
import com.arkadiusz.dayscounter.utils.DateUtils.calculateDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.utils.DateUtils.formatTime
import com.arkadiusz.dayscounter.utils.ThemeUtils
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_detail.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startActivity
import java.io.File


class DetailActivity : AppCompatActivity() {

    private val databaseRepository = DatabaseProvider.provideRepository()

    private var passedEventId: String = ""
    private lateinit var passedEvent: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setStatusBarColor()
        setUpToolbar()
        receiveEventAndCancelNotification()
        displayImage()
        fillMainSection()
        fillAboutSection()
        fillReminderSection()
        fillRepetitionSection()
        displayAd()
    }

    private fun setStatusBarColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_edit -> {
                startActivity<EditActivity>("eventId" to passedEventId)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""
    }

    private fun receiveEventAndCancelNotification() {
        passedEventId = intent.getStringExtra("event_id")
        passedEvent = databaseRepository.getEventById(passedEventId)
        val isComingFromNotification = intent.getStringExtra("notificationClick")

        if (isComingFromNotification != null && isComingFromNotification == "clicked") {
            notificationManager.cancel(passedEventId.hashCode())
        }
    }

    private fun displayImage() {
        when {
            passedEvent.imageColor != 0 -> {
                eventImage.setImageDrawable(null)
                eventImage.backgroundColor = passedEvent.imageColor
            }
            passedEvent.imageID == 0 -> {
                when {
                    File(passedEvent.image).exists() -> Glide.with(this).load(passedEvent.image).into(eventImage)
                    passedEvent.imageCloudPath.isNotEmpty() -> {
                        Glide.with(this).load(
                                FirebaseStorage.getInstance().getReference(passedEvent.imageCloudPath))
                                .into(eventImage)
                    }
                    else -> {

                    }
                }
            }
            else -> Glide.with(this).load(passedEvent.imageID).into(eventImage)
        }
    }

    private fun fillMainSection() {
        val counterText = calculateDate(passedEvent.date,
                passedEvent.formatYearsSelected,
                passedEvent.formatMonthsSelected,
                passedEvent.formatWeeksSelected,
                passedEvent.formatDaysSelected, this)
        eventCalculateText.text = counterText
        eventTitle.text = passedEvent.name
    }

    private fun fillAboutSection() {
        dateText.text = formatDateAccordingToSettings(passedEvent.date,
                defaultPrefs(this)["dateFormat"] ?: "")
        if (passedEvent.description.isNotEmpty()) {
            descriptionText.text = passedEvent.description
        } else {
            descriptionText.visibility = View.GONE
        }
    }

    private fun fillRepetitionSection() {
        repeatSectionText.text = when (passedEvent.repeat) {
            "0" -> getString(R.string.detail_once)
            "1" -> getString(R.string.detail_daily)
            "2" -> getString(R.string.detail_weekly)
            "3" -> getString(R.string.detail_monthly)
            "4" -> getString(R.string.detail_yearly)
            else -> ""
        }
    }

    private fun fillReminderSection() {
        if (passedEvent.reminderYear != 0) {
            val reminderDate = "${formatDateAccordingToSettings(formatDate(passedEvent.reminderYear,
                    passedEvent.reminderMonth,
                    passedEvent.reminderDay),
                    defaultPrefs(this)["dateFormat"] ?: "")} " +
                    formatTime(passedEvent.reminderHour, passedEvent.reminderMinute)

            reminderSectionText.text = reminderDate
            reminderDescriptionText.text = passedEvent.notificationText
        } else {
            reminderDescriptionText.visibility = View.GONE
        }
    }


    private fun displayAd() {
        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean? = prefs["ads", false]
        if (areAdsRemoved != true) {
            adView.loadAd(AdRequest.Builder().build())
        } else {
            adView.visibility = View.GONE
            scrollN.setPadding(0, 0, 0, 0)
        }
    }
}