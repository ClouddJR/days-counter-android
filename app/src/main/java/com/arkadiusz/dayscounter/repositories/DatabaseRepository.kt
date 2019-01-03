package com.arkadiusz.dayscounter.repositories

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.content.Context
import android.net.Uri
import android.util.Log.d
import com.arkadiusz.dayscounter.database.Migration
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.generateCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.getDateForBackupFile
import com.arkadiusz.dayscounter.utils.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.utils.StorageUtils.BACKUP_PATH
import com.arkadiusz.dayscounter.utils.StorageUtils.EXPORT_FILE_EXTENSION
import com.arkadiusz.dayscounter.utils.StorageUtils.EXPORT_FILE_NAME
import com.arkadiusz.dayscounter.utils.StorageUtils.toFile
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import java.io.File
import java.util.*

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DatabaseRepository {


    private var realm: Realm
    private val config: RealmConfiguration = RealmConfiguration.Builder()
            .schemaVersion(3)
            .migration(Migration())
            .build()

    object RealmInitializer {
        fun initRealm(context: Context) {
            Realm.init(context)
        }
    }

    init {
        realm = Realm.getInstance(config)
    }

    fun logEvents() {
        val results = realm.where(Event::class.java).findAll()
        for (event in results) {
            d("eventRepository", event.toString() + "\n")
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
        return realm.where(Event::class.java).equalTo("name", name).findFirst()!!
    }

    fun getEventById(id: Int): Event {
        return realm.where(Event::class.java).equalTo("id", id).findFirst()!!
    }

    fun getEventByWidgetId(widgetId: Int): Event? {
        realm.where(Event::class.java).equalTo("widgetID", widgetId).findFirst()?.let {
            return realm.copyFromRealm(it)
        }

        return null
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

    fun setInTransparentWidget(event: Event) {
        realm.executeTransaction {
            event.hasTransparentWidget = false
        }
    }

    fun disableAlarmForEvent(eventId: Int) {
        realm.executeTransaction {
            val event = realm.where(Event::class.java).equalTo("id", eventId).findFirst()!!
            event.hasAlarm = false
            event.reminderYear = 0
            event.reminderMonth = 0
            event.reminderDay = 0
            event.reminderHour = 0
            event.reminderMinute = 0
            event.notificationText = ""
        }
    }

    fun addEventToDatabase(event: Event): Int {
        event.id = getNextId()
        realm.executeTransaction {
            it.copyToRealmOrUpdate(event)
        }
        return event.id
    }

    fun addEventsToDatabase(eventsList: MutableList<Event>) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(eventsList)
        }
    }

    fun editEvent(event: Event) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(event)
        }
    }

    private fun getNextId(): Int {
        var nextId = 1
        try {
            nextId = realm.where(Event::class.java).max("id")!!.toInt() + 1
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

    fun deleteAllEventsFromDatabase() {
        realm.executeTransaction {
            it.deleteAll()
        }
    }

    fun moveEventToPast(eventToBeMoved: Event) {
        val id = eventToBeMoved.id
        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()!!
            event.type = "past"
        }
    }

    fun moveEventToFuture(eventToBeMoved: Event) {
        val id = eventToBeMoved.id
        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()!!
            event.type = "future"
        }
    }

    fun repeatEvent(event: Event) {
        val dateElements = getElementsFromDate(event.date)
        val year = dateElements.first
        val month = dateElements.second
        val day = dateElements.third

        val eventCalendar = generateCalendar(year, month, day)

        when (event.repeat) {
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

    fun backupData(): String {
        val backupFolder = File(BACKUP_PATH)
        backupFolder.mkdir()

        val file = File(BACKUP_PATH, "${EXPORT_FILE_NAME}_${getDateForBackupFile()}.$EXPORT_FILE_EXTENSION")
        file.delete()
        realm.writeCopyTo(file)

        return BACKUP_PATH
    }

    fun importData(context: Context, uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            realm.close()
            Realm.deleteRealm(config)
            it.toFile("${context.filesDir}/default.realm")
            realm = Realm.getInstance(config)

            FirebaseRepository().processSyncOperationFor(defaultPrefs(context)["firebase-email"]
                    ?: "")

        }
    }

}