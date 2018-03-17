package com.arkadiusz.dayscounter.database

import com.google.firebase.database.IgnoreExtraProperties
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by arkadiusz on 04.03.18
 */

@IgnoreExtraProperties
class Event_r : RealmObject() {

    @PrimaryKey
    private var id: Int = 0
    private var name: String = ""
    private var date: String = ""
    private var description: String = ""
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var notificationText: String = ""
    private var hasAlarm: Boolean = false
    private var repeat: String = ""
    private var imagePath: String = ""
    private var imageID: Int = 0
    private var type: String? = ""
    private var widgetID: Int = 0
    private var color: Int = 0

}