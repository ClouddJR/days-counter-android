package com.arkadiusz.dayscounter.repositories

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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DatabaseRepository {

    private val userRepository = UserRepository()
    private val firebaseRepository = FirebaseRepository(userRepository)

    private var realm: Realm
    private val config: RealmConfiguration = RealmConfiguration.Builder()
            .schemaVersion(4)
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

    fun getEventsWithWidgets(): List<Event> {
        val realm = Realm.getInstance(config)
        val list = realm.copyFromRealm(
                realm.where(Event::class.java).notEqualTo("widgetID", 0L).findAll()
        )
        realm.close()
        return list
    }

    private fun getCopyOfAllEvents(): List<Event> {
        return realm.copyFromRealm(realm.where(Event::class.java).findAll())
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

    fun getEventById(id: String): Event? {
        return realm.where(Event::class.java).equalTo("id", id).findFirst()
    }

    fun getEventByWidgetId(widgetId: Int): Event? {
        val realm = Realm.getInstance(config)
        val event = realm.where(Event::class.java).equalTo("widgetID", widgetId).findFirst()
        return if (event != null) {
            val eventCopy = realm.copyFromRealm(event)
            realm.close()
            eventCopy
        } else {
            null
        }
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

    fun disableAlarmForEvent(eventId: String) {
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

    fun addEventToDatabase(event: Event): String {
        event.id = getNextId()

        //set up image path in firebase
        if (event.image.isNotEmpty() && userRepository.isLoggedIn()) {
            val imageName = Uri.parse(event.image).lastPathSegment
            val path = "${userRepository.getUserId()}/${event.id}/$imageName"
            event.imageCloudPath = path
        }

        realm.executeTransactionAsync(Realm.Transaction {

            it.copyToRealmOrUpdate(event)

        }, Realm.Transaction.OnSuccess {
            if (userRepository.isLoggedIn()) {
                Timer("Firebase", false).schedule(1000) {
                    if (event.image.isNotEmpty()) {
                        firebaseRepository.addImageForEvent(event)
                    }
                    firebaseRepository.addEvent(event)
                }
            }
        })


        return event.id
    }

    fun editEvent(event: Event) {
        val oldEvent = realm.copyFromRealm(realm.where(Event::class.java)
                .equalTo("id", event.id).findFirst()!!)

        //set up image path in firebase
        if (userRepository.isLoggedIn()) {

            Timer("Firebase", false).schedule(1000) {
                //previously background or color, now image from sdcard
                if ((oldEvent.imageColor != 0 || oldEvent.imageID != 0) && event.image.isNotEmpty()) {
                    event.imageCloudPath = getCloudImagePath(event)
                    firebaseRepository.addImageForEvent(event)
                }

                //previously image, now background or color
                if ((oldEvent.image.isNotEmpty() || oldEvent.imageCloudPath.isNotEmpty()) &&
                        (event.imageID != 0 || event.imageColor != 0)) {
                    firebaseRepository.deleteImageForEvent(oldEvent)
                }

                //previously image, now the same image
                if (oldEvent.image.isNotEmpty() && event.image == "null" && oldEvent.imageCloudPath == event.imageCloudPath) {

                }

                //previously image, now different one
                if (oldEvent.image != event.image && oldEvent.imageCloudPath == event.imageCloudPath
                        && oldEvent.image.isNotEmpty() && event.image != "null") {
                    firebaseRepository.deleteImageForEvent(oldEvent)
                    event.imageCloudPath = getCloudImagePath(event)
                    firebaseRepository.addImageForEvent(event)
                }

                firebaseRepository.addEvent(event)
            }
        }

        realm.executeTransactionAsync {
            it.copyToRealmOrUpdate(event)
        }
    }

    private fun getCloudImagePath(event: Event): String {
        val imageName = Uri.parse(event.image).lastPathSegment
        return "${userRepository.getUserId()}/${event.id}/$imageName"
    }


    fun deleteEventFromDatabase(eventId: String) {
        lateinit var eventCopy: Event

        realm.executeTransaction {
            val event = it.where(Event::class.java).equalTo("id", eventId).findFirst()!!
            eventCopy = it.copyFromRealm(event)
            event.deleteFromRealm()
        }

        if (userRepository.isLoggedIn()) {
            Timer("Firebase", false).schedule(1000) {
                if (eventCopy.imageCloudPath.isNotEmpty()) {
                    firebaseRepository.deleteImageForEvent(eventCopy)
                }
                firebaseRepository.deleteEvent(eventCopy)
            }
        }
    }

    private fun getNextId(): String {
        return firebaseRepository.getNewId()
    }

    fun moveEventToPast(eventToBeMoved: Event) {
        val id = eventToBeMoved.id

        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()!!
            event.type = "past"

            if (userRepository.isLoggedIn()) {
                firebaseRepository.addEvent(event)
            }
        }
    }

    fun moveEventToFuture(eventToBeMoved: Event) {
        val id = eventToBeMoved.id

        realm.executeTransactionAsync {
            val event = it.where(Event::class.java).equalTo("id", id).findFirst()!!
            event.type = "future"

            if (userRepository.isLoggedIn()) {
                firebaseRepository.addEvent(event)
            }
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

        val id = event.id
        realm.executeTransactionAsync {
            val eventToBeRepeated = it.where(Event::class.java).equalTo("id", id).findFirst()!!
            eventToBeRepeated.date = dateAfterRepetition

            if (userRepository.isLoggedIn()) {
                firebaseRepository.addEvent(eventToBeRepeated)
            }
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

            if (userRepository.isLoggedIn()) {
                addLocalEventsToCloud()
            }
        }
    }

    fun addLocalEventsToCloud() {
        val events = getCopyOfAllEvents()
        events.forEach {
            firebaseRepository.addImageForEvent(it)
            firebaseRepository.addEvent(it)
        }
    }

    fun syncToCloud(context: Context?) {
        if (userRepository.isLoggedIn() && FirebaseRepository.isNetworkEnabled(context)) {
            observeCloudEvents()
        }
    }

    private fun observeCloudEvents() {
        val disposable = firebaseRepository.getEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { events: List<Event> ->

                            deleteIfNotExist(events)

                            events.forEach { event ->
                                editCloudEvent(event)
                            }

                        },
                        { error: Throwable ->
                            error.printStackTrace()

                        }
                )
    }

    private fun deleteIfNotExist(passedEvents: List<Event>) {
        val events = getAllEvents()
        events.forEach { localEvent ->
            if (passedEvents.none { it.id == localEvent.id }) {
                realm.executeTransaction {
                    localEvent.deleteFromRealm()
                }
            }
        }
    }

    private fun editCloudEvent(event: Event) {
        realm.executeTransactionAsync {
            val existingEvent = it.where(Event::class.java).equalTo("id", event.id).findFirst()

            if (existingEvent != null) {
                if (!existingEvent.isTheSameAs(event)) {
                    existingEvent.copyValuesFrom(event)
                }
            } else {
                it.copyToRealmOrUpdate(event)
            }

        }
    }

}