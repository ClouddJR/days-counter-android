package com.arkadiusz.dayscounter.data.local

import android.content.Context
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.utils.StorageUtils.toFile
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.io.File
import java.io.InputStream

class LocalDatabase {

    private var realm: Realm
    private val config: RealmConfiguration = RealmConfiguration.Builder()
            .schemaVersion(4)
            .migration(Migration())
            .build()

    init {
        realm = Realm.getInstance(config)
    }

    fun getAllEvents(): RealmResults<Event> {
        return realm.where(Event::class.java).findAll()
    }

    fun getEventsWithWidgets(): List<Event> {
        return realm.where(Event::class.java).notEqualTo("widgetID", 0L).findAll()
    }

    fun getCopyOfAllEvents(): List<Event> {
        return realm.copyFromRealm(realm.where(Event::class.java).findAll())
    }

    fun getFutureEvents(): RealmResults<Event> {
        return realm.where(Event::class.java).equalTo("type", "future").findAll()
    }

    fun getPastEvents(): RealmResults<Event> {
        return realm.where(Event::class.java).equalTo("type", "past").findAll()
    }

    fun getEventsWithAlarms(): RealmResults<Event> {
        return realm.where(Event::class.java).equalTo("hasAlarm", true).findAll()
    }

    fun getEventById(id: String): Event? {
        return realm.where(Event::class.java).equalTo("id", id).findFirst()
    }

    fun getEventCopyById(eventId: String): Event {
        return realm.copyFromRealm(realm.where(Event::class.java).equalTo("id", eventId).findFirst()!!)
    }

    fun getEventByWidgetId(widgetId: Int): Event? {
        return realm.where(Event::class.java).equalTo("widgetID", widgetId).findFirst()
    }

    fun setWidgetIdForEvent(event: Event, widgetId: Int) {
        realm.executeTransaction {
            event.widgetID = widgetId
        }
    }

    fun setWidgetTransparencyFor(event: Event, isTransparent: Boolean) {
        realm.executeTransaction {
            event.hasTransparentWidget = isTransparent
        }
    }

    fun disableAlarmForEvent(eventId: String) {
        realm.executeTransaction {
            val event = getEventById(eventId)
            event?.hasAlarm = false
            event?.reminderYear = 0
            event?.reminderMonth = 0
            event?.reminderDay = 0
            event?.reminderHour = 0
            event?.reminderMinute = 0
            event?.notificationText = ""
        }
    }

    fun addOrUpdateEvent(event: Event) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(event)
        }
    }

    fun deleteEvent(event: Event) {
        realm.executeTransactionAsync {
            it.where(Event::class.java).equalTo("id", event.id).findFirst()?.deleteFromRealm()
        }
    }

    fun moveEventToPast(eventToBeMoved: Event) {
        val id = eventToBeMoved.id
        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()
            event?.type = "past"
        }
    }

    fun moveEventToFuture(eventToBeMoved: Event) {
        val id = eventToBeMoved.id
        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()
            event?.type = "future"
        }
    }

    fun repeatEvent(eventToBeRepeated: Event, dateAfterRepetition: String) {
        val id = eventToBeRepeated.id
        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()
            event?.date = dateAfterRepetition
        }
    }

    fun writeCopyToFile(file: File) {
        realm.writeCopyTo(file)
    }

    fun importData(context: Context, inputStream: InputStream) {
        realm.close()
        Realm.deleteRealm(config)
        inputStream.toFile("${context.filesDir}/default.realm")
        realm = Realm.getInstance(config)
    }

    fun closeDatabase() {
        realm.close()
    }

    fun updateLocalEventBasedOn(cloudEvent: Event) {
        realm.executeTransactionAsync {
            val existingEvent = it.where(Event::class.java).equalTo("id", cloudEvent.id).findFirst()

            if (existingEvent != null) {
                if (!existingEvent.isTheSameAs(cloudEvent)) {
                    existingEvent.copyValuesFrom(cloudEvent)
                }
            } else {
                it.copyToRealmOrUpdate(cloudEvent)
            }
        }
    }
}