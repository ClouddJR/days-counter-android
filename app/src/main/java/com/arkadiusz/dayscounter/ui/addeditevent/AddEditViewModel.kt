package com.arkadiusz.dayscounter.ui.addeditevent

import android.content.res.Resources
import androidx.lifecycle.*
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val resources: Resources
) : ViewModel() {

    private var previousEvent: Event? = null

    private val uiLiveData = MutableLiveData<EventUIData>()

    private val currentUiData
        get() = uiLiveData.value!!

    val nameLiveData: LiveData<String> =
        uiLiveData.map { it.name }.distinctUntilChanged()

    val dateLiveData: LiveData<String> =
        uiLiveData.map { it.date }.distinctUntilChanged()

    val counterTextLiveData: LiveData<String> =
        uiLiveData.map {
            DateUtils.calculateDate(
                currentUiData.date,
                currentUiData.selection.formatYearsSelected,
                currentUiData.selection.formatMonthsSelected,
                currentUiData.selection.formatWeeksSelected,
                currentUiData.selection.formatDaysSelected,
                resources
            )
        }.distinctUntilChanged()

    val descriptionLiveData: LiveData<String> =
        uiLiveData.map { it.description }.distinctUntilChanged()

    val selectionLiveData: LiveData<UISelection> =
        uiLiveData.map { it.selection }.distinctUntilChanged()

    val imageLiveData: LiveData<EventImage> =
        uiLiveData.map { it.image }.distinctUntilChanged()

    val reminderComponents: LiveData<ReminderComponents> =
        uiLiveData.map { it.reminderComponents }.distinctUntilChanged()

    val notificationTextLiveData: LiveData<String> =
        uiLiveData.map { it.notificationText }.distinctUntilChanged()

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    fun init(type: String) {
        initUIData(previousEvent = null, type)
    }

    fun initWithPreviousEvent(eventId: String) {
        val event = getEventById(eventId).also { previousEvent = it }
        initUIData(previousEvent = event, event.type)
    }

    fun saveEvent() = databaseRepository.addEvent(prepareEvent())

    fun editEvent() = databaseRepository.editEvent(prepareEvent())

    fun updateName(name: String) {
        uiLiveData.value = currentUiData.copy(name = name)
    }

    fun getCurrentDate() = currentUiData.date

    fun updateDate(date: String) {
        uiLiveData.value = currentUiData.copy(date = date)
    }

    fun updateDescription(description: String) {
        uiLiveData.value = currentUiData.copy(description = description)
    }

    fun updateRepetitionSelection(selection: Int) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(repetitionSelection = selection)
        )
    }

    fun updateFormatYearsSelection(selected: Boolean) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(formatYearsSelected = selected)
        )
    }

    fun updateFormatMonthsSelection(selected: Boolean) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(formatMonthsSelected = selected)
        )
    }

    fun updateFormatWeeksSelection(selected: Boolean) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(formatWeeksSelected = selected)
        )
    }

    fun updateFormatDaysSelection(selected: Boolean) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(formatDaysSelected = selected)
        )
    }

    fun updateLineDividerSelection(selected: Boolean) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(lineDividerSelected = selected)
        )
    }

    fun updateCounterFontSize(size: Int) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(counterFontSize = size)
        )
    }

    fun updateTitleFontSize(size: Int) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(titleFontSize = size)
        )
    }

    fun updateFontType(type: String) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(fontType = type)
        )
    }

    fun getSelectedFontColor() = currentUiData.selection.fontColor

    fun updateFontColor(color: Int) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(fontColor = color)
        )
    }

    fun updateDimValue(value: Int) {
        uiLiveData.value = currentUiData.copy(
            selection = currentUiData.selection.copy(dimValue = value)
        )
    }

    fun getCurrentImage(): EventImage = currentUiData.image

    fun updateImageToBackgroundColor(color: Int) {
        uiLiveData.value = currentUiData.copy(image = ColorBackground(color))
    }

    fun updateImageToLocalFile(path: String) {
        uiLiveData.value = currentUiData.copy(image = LocalFile(path))
    }

    fun updateImageToLocalGalleryItem(imageId: Int) {
        uiLiveData.value = currentUiData.copy(image = LocalGalleryImage(imageId))
    }

    fun clearReminder() {
        uiLiveData.value = currentUiData.copy(
            reminderComponents = ReminderComponents(0, 0, 0, 0, 0)
        )
    }

    fun getCurrentReminderComponents() = currentUiData.reminderComponents

    fun isReminderSet() = getCurrentReminderComponents().year != 0

    fun updateReminderDateComponents(year: Int, month: Int, day: Int) {
        uiLiveData.value = currentUiData.copy(
            reminderComponents = currentUiData.reminderComponents.copy(
                year = year,
                month = month,
                day = day
            )
        )
    }

    fun updateReminderTimeComponents(hour: Int, minute: Int) {
        uiLiveData.value = currentUiData.copy(
            reminderComponents = currentUiData.reminderComponents.copy(
                hour = hour,
                minute = minute,
            )
        )
    }

    fun updateNotificationText(notificationText: String) {
        uiLiveData.value = currentUiData.copy(notificationText = notificationText)
    }

    private fun initUIData(previousEvent: Event?, type: String) {
        uiLiveData.value = EventUIData(
            name = previousEvent?.name ?: resources.getString(R.string.add_activity_preview_title),
            date = previousEvent?.date ?: currentDate(),
            description = previousEvent?.description ?: "",
            type = type,
            selection = UISelection(
                repetitionSelection = previousEvent?.repeat?.toInt()
                    ?: DEFAULT_REPETITION_SELECTION,
                formatYearsSelected = previousEvent?.formatYearsSelected ?: false,
                formatMonthsSelected = previousEvent?.formatMonthsSelected ?: false,
                formatWeeksSelected = previousEvent?.formatWeeksSelected ?: false,
                formatDaysSelected = previousEvent?.formatDaysSelected ?: true,
                lineDividerSelected = previousEvent?.lineDividerSelected ?: true,
                counterFontSize = previousEvent?.counterFontSize ?: DEFAULT_COUNTER_FONT_SIZE,
                titleFontSize = previousEvent?.titleFontSize ?: DEFAULT_TITLE_FONT_SIZE,
                fontType = previousEvent?.fontType ?: DEFAULT_FONT_TYPE_NAME,
                fontColor = previousEvent?.fontColor ?: DEFAULT_FONT_COLOR,
                dimValue = previousEvent?.pictureDim ?: DEFAULT_PICTURE_DIM
            ),
            image = prepareInitialImage(previousEvent),
            reminderComponents = ReminderComponents(
                year = previousEvent?.reminderYear ?: 0,
                month = previousEvent?.reminderMonth ?: 0,
                day = previousEvent?.reminderDay ?: 0,
                hour = previousEvent?.reminderHour ?: 0,
                minute = previousEvent?.reminderMinute ?: 0
            ),
            notificationText = previousEvent?.notificationText ?: "",
        )
    }

    private fun currentDate(): String {
        return with(Calendar.getInstance()) {
            DateUtils.formatDate(
                get(Calendar.YEAR),
                get(Calendar.MONTH),
                get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    private fun prepareInitialImage(previousEvent: Event?): EventImage {
        return when (previousEvent == null) {
            true -> LocalGalleryImage(DEFAULT_IMAGE_ID)
            false -> {
                when {
                    previousEvent.imageColor != 0 -> ColorBackground(previousEvent.imageColor)
                    previousEvent.imageID != 0 -> LocalGalleryImage(previousEvent.imageID)
                    File(previousEvent.image).exists() -> LocalFile(previousEvent.image)
                    previousEvent.imageCloudPath.isNotEmpty() -> CloudFile(
                        previousEvent.image,
                        previousEvent.imageCloudPath
                    )
                    else -> ColorBackground(android.R.color.darker_gray)
                }
            }
        }
    }

    private fun getEventById(eventId: String) = databaseRepository.getEventById(eventId)!!

    private fun prepareEvent(): Event {
        val uiData = uiLiveData.value!!

        Event().apply {
            id = previousEvent?.id ?: ""
            name = uiData.name
            date = uiData.date
            description = uiData.description
            type = uiData.type
            repeat = uiData.selection.repetitionSelection.toString()
            reminderYear = uiData.reminderComponents.year
            reminderMonth = uiData.reminderComponents.month
            reminderDay = uiData.reminderComponents.day
            reminderHour = uiData.reminderComponents.hour
            reminderMinute = uiData.reminderComponents.minute
            notificationText = uiData.notificationText
            imageCloudPath = previousEvent?.imageCloudPath ?: ""
            widgetID = previousEvent?.widgetID ?: 0
            hasTransparentWidget = previousEvent?.hasTransparentWidget ?: false
            formatYearsSelected = uiData.selection.formatYearsSelected
            formatMonthsSelected = uiData.selection.formatMonthsSelected
            formatWeeksSelected = uiData.selection.formatWeeksSelected
            formatDaysSelected = uiData.selection.formatDaysSelected
            lineDividerSelected = uiData.selection.lineDividerSelected
            counterFontSize = uiData.selection.counterFontSize
            titleFontSize = uiData.selection.titleFontSize
            fontType = uiData.selection.fontType
            fontColor = uiData.selection.fontColor
            pictureDim = uiData.selection.dimValue
        }.also { event ->
            assignImageRelatedFields(event, uiData)
            return event
        }
    }

    private fun assignImageRelatedFields(event: Event, uiData: EventUIData) {
        when (val image = uiData.image) {
            is ColorBackground -> {
                event.image = ""
                event.imageColor = image.color
                event.imageID = 0
            }
            is CloudFile -> {
                event.image = image.localPath
                event.imageColor = 0
                event.imageID = 0
            }
            is LocalFile -> {
                event.image = image.path
                event.imageColor = 0
                event.imageID = 0
            }
            is LocalGalleryImage -> {
                event.image = ""
                event.imageColor = 0
                event.imageID = image.resourceId
            }
        }
    }

    private companion object {
        const val DEFAULT_REPETITION_SELECTION = 0
        const val DEFAULT_COUNTER_FONT_SIZE = 20
        const val DEFAULT_TITLE_FONT_SIZE = 18
        const val DEFAULT_FONT_TYPE_NAME = "Roboto"
        const val DEFAULT_FONT_COLOR = -1
        const val DEFAULT_PICTURE_DIM = 4
        const val DEFAULT_IMAGE_ID = 2131230778
    }
}
