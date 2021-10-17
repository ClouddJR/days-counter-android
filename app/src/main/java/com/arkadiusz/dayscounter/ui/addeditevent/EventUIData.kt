package com.arkadiusz.dayscounter.ui.addeditevent

data class EventUIData(
    val name: String,
    val date: String,
    val description: String,
    val type: String,
    val selection: UISelection,
    val image: EventImage,
    val reminderComponents: ReminderComponents,
    val notificationText: String,
    val imageCloudPath: String,
    val widgetId: Int,
    val hasTransparentWidget: Boolean
)

data class UISelection(
    val repetitionSelection: Int,
    val formatYearsSelected: Boolean,
    val formatMonthsSelected: Boolean,
    val formatWeeksSelected: Boolean,
    val formatDaysSelected: Boolean,
    val lineDividerSelected: Boolean,
    val counterFontSize: Int,
    val titleFontSize: Int,
    val fontType: String,
    val fontColor: Int,
    val dimValue: Int,
)

sealed class EventImage
class ColorBackground(color: Int): EventImage()
class LocalGalleryImage(resourceId: Int): EventImage()
class LocalFile(path: String): EventImage()
class CloudFile(path: String): EventImage()

data class ReminderComponents(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)
