package com.arkadiusz.dayscounter.repositories

import android.content.Context
import android.util.Log.d
import com.arkadiusz.dayscounter.database.Event
import com.arkadiusz.dayscounter.model.Migration
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.generateCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.getElementsFromDate
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DatabaseRepository {

    private var realm: Realm

    object RealmInitializer {
        fun initRealm(context: Context) {
            Realm.init(context)
        }
    }

    init {
        val config = RealmConfiguration.Builder()
                .schemaVersion(3)
                .migration(Migration())
                .build()
        realm = Realm.getInstance(config)
    }

    fun logEvents() {
        val results = realm.where(Event::class.java).findAll()
        for (event in results) {
            d("eventRepository", event.toString())
        }
    }

    fun getAllEvents(): RealmResults<Event> {
        lateinit var results: RealmResults<Event>
        realm.executeTransaction {
            results = it.where(Event::class.java).findAll()
        }
        return results
    }

    fun getFutureEvents(): RealmResults<Event> {
        lateinit var results: RealmResults<Event>
        realm.executeTransaction {
            results = it.where(Event::class.java).equalTo("type", "future").findAll()
        }
        return results
    }

    fun getPastEvents(): RealmResults<Event> {
        lateinit var results: RealmResults<Event>
        realm.executeTransaction {
            results = it.where(Event::class.java).equalTo("type", "past").findAll()
        }
        return results
    }

    fun getEventsWithAlarms(): RealmResults<Event> {
        lateinit var results: RealmResults<Event>
        realm.executeTransaction {
            results = it.where(Event::class.java).equalTo("hasAlarm", true).findAll()
        }
        return results
    }

    fun sortEventsDateDesc(data: RealmResults<Event>): RealmResults<Event> {
        return data.sort("date", Sort.DESCENDING)
    }

    fun sortEventsDateAsc(data: RealmResults<Event>): RealmResults<Event> {
        return data.sort("date", Sort.ASCENDING)
    }

    fun getEventByName(name: String): Event {
        return realm.where(Event::class.java).equalTo("name", name).findFirst()
    }

    fun getEventByWidgetId(widgetId: Int): Event? {
        return realm.where(Event::class.java).equalTo("widgetID", widgetId).findFirst()
    }

    fun setWidgetIdForEvent(event: Event, widgetId: Int) {
        realm.executeTransaction {
            event.widgetID = widgetId
        }
    }

    fun setTransparentWidget(event: Event) {
        realm.executeTransaction {
            event.hasTransparentWidget = true
        }
    }

    fun disableAlarmForEvent(eventId: Int) {
        realm.executeTransaction {
            val event = realm.where(Event::class.java).equalTo("id", eventId).findFirst()
            event.hasAlarm = false
        }
    }

    fun addEventToDatabase(event: Event) {
        event.id = getNextId()
        realm.executeTransaction {
            it.copyToRealmOrUpdate(event)
        }
    }

    private fun getNextId(): Int {
        var nextId = 1
        try {
            nextId = realm.where(Event::class.java).max("id").toInt() + 1
        } catch (e: NullPointerException) {
            return nextId
        }
        return nextId
    }

    fun deleteEventFromDatabase(eventId: Int) {
        val results = realm.where(Event::class.java).equalTo("id", eventId).findAll()
        realm.executeTransaction {
            results.deleteAllFromRealm()
        }
    }

    fun moveEventToPast(eventToBeMoved: Event) {
        realm.executeTransaction {
            val event = it.where(Event::class.java).equalTo("id", eventToBeMoved.id).findFirst()
            event.type = "past"
        }
    }

    fun moveEventToFuture(eventToBeMoved: Event) {
        realm.executeTransaction {
            val event = it.where(Event::class.java).equalTo("id", eventToBeMoved.id).findFirst()
            event.type = "future"
        }
    }

    fun repeatEvent(event: Event) {
        val dateElements = getElementsFromDate(event.date)
        val year = dateElements.first
        val month = dateElements.second
        val day = dateElements.third

        val eventCalendar = generateCalendar(year, month, day)

        when (event.type) {
            "1" -> eventCalendar.add(Calendar.DAY_OF_MONTH, 1)
            "2" -> eventCalendar.add(Calendar.DAY_OF_MONTH, 7)
            "3" -> eventCalendar.add(Calendar.MONTH, 1)
            "4" -> eventCalendar.add(Calendar.YEAR, 1)
        }

        val dateAfterRepetition = formatDate(eventCalendar.get(Calendar.YEAR),
                eventCalendar.get(Calendar.MONTH),
                eventCalendar.get(Calendar.DAY_OF_MONTH))

        realm.executeTransaction {
            event.date = dateAfterRepetition
        }
    }
}