package com.arkadiusz.dayscounter.ui.addeditevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.util.DateUtils
import java.io.File
import java.util.*

class AddEditViewModel(
    private val databaseRepository: DatabaseRepository = DatabaseRepository()
) : ViewModel() {

    private var previousEvent: Event? = null

    private val uiLiveData = MutableLiveData<EventUIData>()

    val nameLiveData: LiveData<String> = uiLiveData.map { it.name }
    val dateLiveData: LiveData<String> = uiLiveData.map { it.date }
    val descriptionLiveData: LiveData<String> = uiLiveData.map { it.description }
    val selectionLiveData: LiveData<UISelection> = uiLiveData.map { it.selection }
    val imageLiveData: LiveData<EventImage> = uiLiveData.map { it.image }
    val reminderComponents: LiveData<ReminderComponents> = uiLiveData.map { it.reminderComponents }
    val notificationTextLiveData: LiveData<String> = uiLiveData.map { it.notificationText }

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

    private fun initUIData(previousEvent: Event?, type: String) {
        uiLiveData.value = EventUIData(
            name = previousEvent?.name ?: "",
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
            DateUtils.formatDate(get(Calendar.YEAR), Calendar.MONTH, Calendar.DAY_OF_MONTH)
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
