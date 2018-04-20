package com.arkadiusz.dayscounter.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.DateUtils.calculateDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatTime
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_detail.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startActivity


class DetailActivity : AppCompatActivity() {

    private val databaseRepository = DatabaseRepository()

    private var passedEventId: Int = 0
    private lateinit var passedEvent: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        receiveEventAndCancelNotification()
        displayImage()
        fillMainSection()
        fillAboutSection()
        fillReminderSection()
        fillRepetitionSection()
        setUpOnFABClickListener()
    }

    private fun receiveEventAndCancelNotification() {
        passedEventId = intent.getIntExtra("event_id", 1)
        passedEvent = databaseRepository.getEventById(passedEventId)
        val isComingFromNotification = intent.getStringExtra("notificationClick")

        if (isComingFromNotification != null && isComingFromNotification == "clicked") {
            notificationManager.cancel(passedEventId)
        }
    }

    private fun displayImage() {
        when {
            passedEvent.imageColor != 0 -> {
                eventImage.setImageDrawable(null)
                eventImage.backgroundColor = passedEvent.imageColor
            }
            passedEvent.imageID == 0 -> Glide.with(this).load(passedEvent.image).into(eventImage)
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
        dateText.text = passedEvent.date
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
            val reminderDate = "${formatDate(passedEvent.reminderYear, passedEvent.reminderMonth, passedEvent.reminderDay)} " +
                    formatTime(passedEvent.reminderHour, passedEvent.reminderMinute)
            reminderSectionText.text = reminderDate
            reminderDescriptionText.text = passedEvent.notificationText
        } else {
            reminderDescriptionText.visibility = View.GONE
        }
    }

    private fun setUpOnFABClickListener() {
        editFAB.setOnClickListener {
            startActivity<EditActivity>("eventId" to passedEventId)
            finish()
        }
    }
}