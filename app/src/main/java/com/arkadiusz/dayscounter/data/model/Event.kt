package com.arkadiusz.dayscounter.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Event : RealmObject() {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var date: String = ""
    var description: String = ""
    var image: String = ""
    var imageCloudPath: String = ""
    var imageID: Int = 0
    var imageColor: Int = 0
    var type: String = ""
    var repeat: String = ""
    var widgetID: Int = 0
    var hasTransparentWidget: Boolean = false
    var reminderYear: Int = 0
    var reminderMonth: Int = 0
    var reminderDay: Int = 0
    var reminderHour: Int = 0
    var reminderMinute: Int = 0
    var notificationText: String = ""
    var formatYearsSelected: Boolean = false
    var formatMonthsSelected: Boolean = false
    var formatWeeksSelected: Boolean = false
    var formatDaysSelected: Boolean = true
    var lineDividerSelected: Boolean = false
    var counterFontSize: Int = 0
    var titleFontSize: Int = 0
    var fontType: String = ""
    var fontColor: Int = 0
    var pictureDim: Int = 0

    fun isTheSameAs(other: Event): Boolean {
        return this.id == other.id &&
                this.name == other.name &&
                this.date == other.date &&
                this.description == other.description &&
                this.image == other.image &&
                this.imageCloudPath == other.imageCloudPath &&
                this.imageID == other.imageID &&
                this.imageColor == other.imageColor &&
                this.type == other.type &&
                this.repeat == other.repeat &&
                this.formatYearsSelected == other.formatYearsSelected &&
                this.formatMonthsSelected == other.formatMonthsSelected &&
                this.formatWeeksSelected == other.formatWeeksSelected &&
                this.formatDaysSelected == other.formatDaysSelected &&
                this.lineDividerSelected == other.lineDividerSelected &&
                this.counterFontSize == other.counterFontSize &&
                this.titleFontSize == other.titleFontSize &&
                this.fontType == other.fontType &&
                this.fontColor == other.fontColor &&
                this.pictureDim == other.pictureDim
    }

    fun copyValuesFrom(event: Event) {
        this.name = event.name
        this.date = event.date
        this.description = event.description
        this.image = event.image
        this.imageCloudPath = event.imageCloudPath
        this.imageID = event.imageID
        this.imageColor = event.imageColor
        this.type = event.type
        this.repeat = event.repeat
        this.formatYearsSelected = event.formatYearsSelected
        this.formatMonthsSelected = event.formatMonthsSelected
        this.formatWeeksSelected = event.formatWeeksSelected
        this.formatDaysSelected = event.formatDaysSelected
        this.lineDividerSelected = event.lineDividerSelected
        this.counterFontSize = event.counterFontSize
        this.titleFontSize = event.titleFontSize
        this.fontType = event.fontType
        this.fontColor = event.fontColor
        this.pictureDim = event.pictureDim
    }
}
