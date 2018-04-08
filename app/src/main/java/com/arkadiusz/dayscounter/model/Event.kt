package com.arkadiusz.dayscounter.model

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by arkadiusz on 04.03.18
 */

open class Event() : RealmObject(), Parcelable {

    @PrimaryKey
    var id: Int = 0
    var name: String = ""
    var date: String = ""
    var description: String = ""
    var image: String = ""
    var imageID: Int = 0
    var imageColor: Int = 0
    var type: String = ""
    var repeat: String = ""
    var widgetID: Int = 0
    var hasAlarm: Boolean = false
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

    constructor(source: Parcel) : this()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {}

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event = Event(source)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }

    }

    override fun toString(): String {
        return "Event(id=$id, " +
                "name='$name', " +
                "date='$date', " +
                "description='$description', " +
                "image='$image', " +
                "imageID=$imageID, " +
                "imageColor=$imageColor, " +
                "type='$type', " +
                "repeat='$repeat', " +
                "widgetID=$widgetID, " +
                "hasAlarm=$hasAlarm, " +
                "hasTransparentWidget=$hasTransparentWidget, " +
                "reminderYear=$reminderYear, " +
                "reminderMonth=$reminderMonth, " +
                "reminderDay=$reminderDay, " +
                "reminderHour=$reminderHour, " +
                "reminderMinute=$reminderMinute, " +
                "notificationText='$notificationText', " +
                "formatYearsSelected=$formatYearsSelected, " +
                "formatMonthsSelected=$formatMonthsSelected, " +
                "formatWeeksSelected=$formatWeeksSelected, " +
                "formatDaysSelected=$formatDaysSelected, " +
                "lineDividerSelected=$lineDividerSelected, " +
                "counterFontSize=$counterFontSize, " +
                "titleFontSize=$titleFontSize, " +
                "fontType='$fontType', " +
                "fontColor=$fontColor, " +
                "pictureDim=$pictureDim)"
    }

}
