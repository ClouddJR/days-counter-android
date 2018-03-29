package com.arkadiusz.dayscounter.database

import com.google.firebase.database.IgnoreExtraProperties
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by arkadiusz on 04.03.18
 */

@IgnoreExtraProperties
open class Event() : RealmObject() {

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

}