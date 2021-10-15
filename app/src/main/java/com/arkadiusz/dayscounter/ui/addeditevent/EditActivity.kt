package com.arkadiusz.dayscounter.ui.addeditevent

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Spinner
import android.widget.TextView
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.util.DateUtils.formatDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.util.DateUtils.formatTime
import com.arkadiusz.dayscounter.util.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.util.FontUtils
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.content_add.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.io.File

class EditActivity : BaseAddEditActivity() {

    private lateinit var passedEvent: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receivePassedEvent()
        fillFormWithPassedData()
        changeAddButtonName()
    }

    override fun handleSaveClick() {
        prepareEventBasedOnViews().also { event ->
            viewModel.editEvent(event)
            addReminder(event)
            updateWidgetIfOnScreen(event.widgetID)
        }
        finish()
    }

    private fun receivePassedEvent() {
        passedEvent = viewModel.getPassedEventById(intent.getStringExtra("eventId")!!)
    }

    private fun fillFormWithPassedData() {
        fillGeneralSectionForm()
        fillReminderSectionForm()
        fillRepetitionSectionForm()
        fillCounterSectionForm()
        fillFontSectionForm()
        fillPictureDimSectionForm()
        displayEventImage()
    }

    private fun fillGeneralSectionForm() {
        titleEditText.setText(passedEvent.name)
        dateEditText.setText(
            formatDateAccordingToSettings(
                passedEvent.date,
                defaultPrefs(this)["dateFormat"] ?: ""
            )
        )
        descriptionEditText.setText(passedEvent.description)
        val dateTriple = getElementsFromDate(passedEvent.date)
        chosenYear = dateTriple.first
        chosenMonth = dateTriple.second - 1
        chosenDay = dateTriple.third
        date = formatDate(chosenYear, chosenMonth, chosenDay)

        eventCalculateText.text = generateCounterText()
    }

    private fun fillReminderSectionForm() {
        if (passedEvent.reminderYear != 0) {
            chosenReminderYear = passedEvent.reminderYear
            chosenReminderMonth = passedEvent.reminderMonth
            chosenReminderDay = passedEvent.reminderDay
            chosenReminderHour = passedEvent.reminderHour
            chosenReminderMinute = passedEvent.reminderMinute
            hasAlarm = true
            val reminderDate = formatDateAccordingToSettings(
                formatDate(
                    passedEvent.reminderYear,
                    passedEvent.reminderMonth,
                    passedEvent.reminderDay
                ), defaultPrefs(this)["dateFormat"] ?: ""
            ) + ", " +
                    formatTime(passedEvent.reminderHour, passedEvent.reminderMinute)

            reminderDateEditText.setText(reminderDate)
            reminderTextEditText.setText(passedEvent.notificationText)
        }
    }

    private fun fillRepetitionSectionForm() {
        repeatSpinner.setSelection(passedEvent.repeat.toInt())
    }

    private fun fillCounterSectionForm() {
        yearsCheckbox.isChecked = passedEvent.formatYearsSelected
        monthsCheckbox.isChecked = passedEvent.formatMonthsSelected
        weeksCheckbox.isChecked = passedEvent.formatWeeksSelected
        daysCheckbox.isChecked = passedEvent.formatDaysSelected
    }

    private fun fillFontSectionForm() {
        showDividerCheckbox.isChecked = passedEvent.lineDividerSelected
        counterFontSizeSpinner.setSelection(
            getSpinnerIndexFor(
                passedEvent.counterFontSize,
                counterFontSizeSpinner
            )
        )
        titleFontSizeSpinner.setSelection(
            getSpinnerIndexFor(
                passedEvent.titleFontSize,
                titleFontSizeSpinner
            )
        )
        fontTypeSpinner.setSelection(FontUtils.getFontPositionFor(passedEvent.fontType))
        colorImageView.backgroundColor = passedEvent.fontColor
        changeWidgetsColors(passedEvent.fontColor)
    }

    private fun getSpinnerIndexFor(fontSize: Int, spinner: Spinner): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == fontSize.toString()) {
                return i
            }
        }
        return 6 //default font size
    }

    private fun changeWidgetsColors(color: Int) {
        selectedColor = color
        eventTitle.textColor = color
        eventCalculateText.textColor = color
        colorImageView.backgroundColor = color
        eventLine.backgroundColor = color
    }

    private fun fillPictureDimSectionForm() {
        pictureDimSeekBar.progress = passedEvent.pictureDim
    }

    private fun displayEventImage() {
        when {
            passedEvent.imageColor != 0 -> changeEventColor(passedEvent.imageColor)
            passedEvent.imageID != 0 -> {
                imageID = passedEvent.imageID
                Glide.with(this).load(imageID).into(eventImage)
            }
            else -> {
                imageUri = Uri.parse(passedEvent.image)
                when {
                    File(passedEvent.image).exists() -> Glide.with(this).load(passedEvent.image)
                        .into(eventImage)
                    passedEvent.imageCloudPath.isNotEmpty() -> Glide.with(this).load(
                        FirebaseStorage.getInstance().getReference(passedEvent.imageCloudPath)
                    )
                        .into(eventImage)
                    else -> Glide.with(this).load(android.R.color.darker_gray)
                        .into(eventImage)
                }
            }
        }
    }

    private fun changeEventColor(color: Int) {
        imageID = 0
        imageUri = null
        imageColor = color
        eventImage.setImageDrawable(null)
        eventImage.backgroundColor = color
    }

    private fun changeAddButtonName() {
        addButton.text = getString(R.string.add_activity_button_title)
    }

    private fun prepareEventBasedOnViews(): Event {
        val event = Event()
        event.id = passedEvent.id
        event.name = titleEditText.text.toString()
        event.date = date
        event.description = descriptionEditText.text.toString()
        event.image = imageUri.toString()
        event.imageID = imageID
        event.imageColor = imageColor
        event.imageCloudPath = passedEvent.imageCloudPath
        event.type = passedEvent.type
        event.repeat = repeatSpinner.selectedItemPosition.toString()
        when (hasAlarm) {
            true -> {
                event.hasAlarm = true
                event.reminderYear = chosenReminderYear
                event.reminderMonth = chosenReminderMonth
                event.reminderDay = chosenReminderDay
                event.reminderHour = chosenReminderHour
                event.reminderMinute = chosenReminderMinute
                event.notificationText = reminderTextEditText.text.toString()
            }
            else -> event.hasAlarm = false
        }
        event.widgetID = passedEvent.widgetID
        event.hasTransparentWidget = passedEvent.hasTransparentWidget
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

    private fun updateWidgetIfOnScreen(id: Int) {
        val intent = Intent(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            null,
            applicationContext,
            AppWidgetProvider::class.java
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
        sendBroadcast(intent)
    }
}