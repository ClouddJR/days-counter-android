package com.arkadiusz.dayscounter.data.local

import android.content.Context
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.util.StorageUtils.toFile
import com.google.firebase.firestore.FirebaseFirestore
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class LocalDatabase @Inject constructor(
    firestore: FirebaseFirestore,
) {

    private var realm: Realm
    private val config: RealmConfiguration = RealmConfiguration.Builder()
        .schemaVersion(5)
        .migration(Migration(firestore))
        .allowWritesOnUiThread(true)
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
        return realm.where(Event::class.java).notEqualTo("reminderYear", 0L).findAll()
    }

    fun getEventById(id: String): Event? {
        return realm.getEventById(id)
    }

    fun getEventCopyById(eventId: String): Event {
        return realm.copyFromRealm(
            realm.getEventById(eventId)!!
        )
    }

    fun getEventByWidgetId(widgetId: Int): Event? {
        return realm.where(Event::class.java).equalTo("widgetID", widgetId).findFirst()
    }

    fun setWidgetIdForEvent(eventId: String, widgetId: Int) {
        realm.executeTransaction {
            it.getEventById(eventId)?.apply {
                widgetID = widgetId
            }
        }
    }

    fun setWidgetTransparencyFor(eventId: String, isTransparent: Boolean) {
        realm.executeTransaction {
            it.getEventById(eventId)?.apply {
                hasTransparentWidget = isTransparent
            }
        }
    }

    fun setLocalImagePath(event: Event, file: File, onFinished: () -> Unit) {
        val id = event.id
        realm.executeTransactionAsync(Realm.Transaction {
            it.getEventById(id)?.apply {
                image = file.path
            }
        }, Realm.Transaction.OnSuccess {
            onFinished()
        })
    }

    fun disableAlarmForEvent(eventId: String) {
        realm.executeTransactionAsync {
            it.getEventById(eventId)?.apply {
                reminderYear = 0
                reminderMonth = 0
                reminderDay = 0
                reminderHour = 0
                reminderMinute = 0
                notificationText = ""
            }
        }
    }

    fun addOrUpdateEvent(event: Event) {
        realm.executeTransactionAsync {
            it.copyToRealmOrUpdate(event)
        }
    }

    fun deleteEvent(event: Event) {
        val id = event.id
        realm.executeTransactionAsync {
            it.getEventById(id)?.deleteFromRealm()
        }
    }

    fun moveEventToPast(eventToBeMoved: Event, onFinished: (() -> Unit)? = null) {
        val id = eventToBeMoved.id
        realm.executeTransactionAsync(Realm.Transaction {
            it.getEventById(id)?.apply {
                type = "past"
            }
        }, Realm.Transaction.OnSuccess {
            onFinished?.let { it() }
        })
    }

    fun moveEventToFuture(eventToBeMoved: Event, onFinished: (() -> Unit)? = null) {
        val id = eventToBeMoved.id
        realm.executeTransactionAsync(Realm.Transaction {
            it.getEventById(id)?.apply {
                type = "future"
            }
        }, Realm.Transaction.OnSuccess {
            onFinished?.let { it() }
        })
    }

    fun repeatEvent(
        eventToBeRepeated: Event, dateAfterRepetition: String,
        onFinished: (() -> Unit)? = null,
    ) {
        val id = eventToBeRepeated.id
        realm.executeTransactionAsync(Realm.Transaction {
            it.getEventById(id)?.apply {
                date = dateAfterRepetition
            }
        }, Realm.Transaction.OnSuccess {
            onFinished?.let { it() }
        })
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

    fun updateLocalEventBasedOn(cloudEvent: Event) {
        realm.executeTransactionAsync {
            val existingEvent = it.getEventById(cloudEvent.id)

            if (existingEvent != null) {
                if (!existingEvent.isTheSameAs(cloudEvent)) {
                    existingEvent.copyValuesFrom(cloudEvent)
                }
            } else {
                it.copyToRealmOrUpdate(cloudEvent)
            }
        }
    }

    fun closeDatabase() {
        realm.close()
    }

    fun isClosed(): Boolean {
        return realm.isClosed
    }

    private fun Realm.getEventById(eventId: String) =
        where(Event::class.java).equalTo("id", eventId).findFirst()
}