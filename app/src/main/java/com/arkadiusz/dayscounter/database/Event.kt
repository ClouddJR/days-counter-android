package com.arkadiusz.dayscounter.database

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by arkadiusz on 04.03.18
 */

@IgnoreExtraProperties
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
    var isLineDividerSelected: Boolean = false
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

}
