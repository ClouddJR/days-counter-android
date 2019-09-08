package com.arkadiusz.dayscounter.data.repository

import android.content.Context
import android.net.Uri
import com.arkadiusz.dayscounter.data.local.LocalDatabase
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.remote.RemoteDatabase
import com.arkadiusz.dayscounter.util.NetworkConnectivityUtils
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.generateCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.getDateForBackupFile
import com.arkadiusz.dayscounter.utils.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.utils.StorageUtils.BACKUP_PATH
import com.arkadiusz.dayscounter.utils.StorageUtils.EXPORT_FILE_EXTENSION
import com.arkadiusz.dayscounter.utils.StorageUtils.EXPORT_FILE_NAME
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.RealmResults
import io.realm.Sort
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DatabaseRepository(
        private val userRepository: UserRepository = UserRepository(),
        private val localDatabase: LocalDatabase = LocalDatabase(),
        private val remoteDatabase: RemoteDatabase = RemoteDatabase(userRepository)
) {
    private lateinit var remoteListenerDisposable: Disposable

    fun getAllEvents(): RealmResults<Event> {
        return localDatabase.getAllEvents()
    }

    fun getEventsWithWidgets(): List<Event> {
        return localDatabase.getEventsWithWidgets()
    }

    private fun getCopyOfAllEvents(): List<Event> {
        return localDatabase.getCopyOfAllEvents()
    }

    fun getFutureEvents(): RealmResults<Event> {
        return localDatabase.getFutureEvents()
    }

    fun getPastEvents(): RealmResults<Event> {
        return localDatabase.getPastEvents()
    }

    fun getEventsWithAlarms(): RealmResults<Event> {
        return localDatabase.getEventsWithAlarms()
    }

    fun sortEventsDateDesc(data: RealmResults<Event>): RealmResults<Event> {
        return data.sort("date", Sort.DESCENDING)
    }

    fun sortEventsDateAsc(data: RealmResults<Event>): RealmResults<Event> {
        return data.sort("date", Sort.ASCENDING)
    }

    fun getEventById(id: String): Event? {
        return localDatabase.getEventById(id)
    }

    fun getEventByWidgetId(widgetId: Int): Event? {
        return localDatabase.getEventByWidgetId(widgetId)
    }

    fun setWidgetIdForEvent(event: Event, widgetId: Int) {
        localDatabase.setWidgetIdForEvent(event, widgetId)
    }

    fun setWidgetTransparencyFor(event: Event, isTransparent: Boolean) {
        localDatabase.setWidgetTransparencyFor(event, isTransparent)
    }

    fun disableAlarmForEvent(eventId: String) {
        localDatabase.disableAlarmForEvent(eventId)
    }

    fun addEvent(event: Event): String {
        event.id = getNextId()
        setUpImagePathForEvent(event)

        localDatabase.addOrUpdateEvent(event)
        if (userRepository.isLoggedIn()) {
            if (event.image.isNotEmpty()) {
                remoteDatabase.addImageForEvent(event)
            }
            remoteDatabase.addOrUpdateEvent(event)
        }

        return event.id
    }

    private fun setUpImagePathForEvent(event: Event) {
        if (event.image.isNotEmpty() && userRepository.isLoggedIn()) {
            val imageName = Uri.parse(event.image).lastPathSegment
            val path = "${userRepository.getUserId()}/${event.id}/$imageName"
            event.imageCloudPath = path
        }
    }

    fun editEvent(event: Event) {
        val oldEvent = localDatabase.getEventCopyById(event.id)

        //set up image path in firebase
        if (userRepository.isLoggedIn()) {

            Timer("Firebase", false).schedule(1000) {
                //previously background or color, now image from sdcard
                if ((oldEvent.imageColor != 0 || oldEvent.imageID != 0) && event.image.isNotEmpty()) {
                    event.imageCloudPath = getCloudImagePath(event)
                    remoteDatabase.addImageForEvent(event)
                }

                //previously image, now background or color
                if ((oldEvent.image.isNotEmpty() || oldEvent.imageCloudPath.isNotEmpty()) &&
                        (event.imageID != 0 || event.imageColor != 0)) {
                    remoteDatabase.deleteImageForEvent(oldEvent)
                }

                //previously image, now the same image
                if (oldEvent.image.isNotEmpty() && event.image == "null" && oldEvent.imageCloudPath == event.imageCloudPath) {

                }

                //previously image, now different one
                if (oldEvent.image != event.image && oldEvent.imageCloudPath == event.imageCloudPath
                        && oldEvent.image.isNotEmpty() && event.image != "null") {
                    remoteDatabase.deleteImageForEvent(oldEvent)
                    event.imageCloudPath = getCloudImagePath(event)
                    remoteDatabase.addImageForEvent(event)
                }

                remoteDatabase.addOrUpdateEvent(event)
            }
        }

        localDatabase.addOrUpdateEvent(event)
    }

    private fun getCloudImagePath(event: Event): String {
        val imageName = Uri.parse(event.image).lastPathSegment
        return "${userRepository.getUserId()}/${event.id}/$imageName"
    }


    fun deleteEventFromDatabase(eventId: String) {
        val eventCopy = localDatabase.getEventCopyById(eventId)
        localDatabase.deleteEvent(eventCopy)

        if (userRepository.isLoggedIn()) {
            //remove associated image stored in cloud
            if (eventCopy.imageCloudPath.isNotEmpty()) {
                remoteDatabase.deleteImageForEvent(eventCopy)
            }
            remoteDatabase.deleteEvent(eventCopy)
        }
    }

    private fun getNextId(): String {
        return remoteDatabase.getNewId()
    }

    fun moveEventToPast(eventToBeMoved: Event) {
        localDatabase.moveEventToPast(eventToBeMoved) {
            if (userRepository.isLoggedIn()) {
                remoteDatabase.addOrUpdateEvent(eventToBeMoved)
            }
        }
    }

    fun moveEventToFuture(eventToBeMoved: Event) {
        localDatabase.moveEventToFuture(eventToBeMoved) {
            if (userRepository.isLoggedIn()) {
                remoteDatabase.addOrUpdateEvent(eventToBeMoved)
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

        localDatabase.repeatEvent(event, dateAfterRepetition) {
            if (userRepository.isLoggedIn()) {
                remoteDatabase.addOrUpdateEvent(event)
            }
        }
    }

    fun backupData(ctx: Context): String {
        val backupPath = StorageUtils.getBackupPath(ctx)
        val backupFolder = File(backupPath)
        backupFolder.mkdir()

        val file = File(backupPath, "${EXPORT_FILE_NAME}_${getDateForBackupFile()}.$EXPORT_FILE_EXTENSION")
        file.delete()
        localDatabase.writeCopyToFile(file)

        return backupPath
    }

    fun importData(context: Context, uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            localDatabase.importData(context, it)

            if (userRepository.isLoggedIn()) {
                addLocalEventsToCloud()
            }
        }
    }

    fun addLocalEventsToCloud() {
        val events = getCopyOfAllEvents()
        events.forEach {
            remoteDatabase.addImageForEvent(it)
            remoteDatabase.addOrUpdateEvent(it)
        }
    }

    fun syncToCloud(context: Context?) {
        if (userRepository.isLoggedIn() && NetworkConnectivityUtils.isNetworkEnabled(context)) {
            fetchCloudEvents()
        }
    }

    private fun fetchCloudEvents() {
        if (::remoteListenerDisposable.isInitialized) {
            remoteListenerDisposable.dispose()
        }
        remoteListenerDisposable = remoteDatabase.getEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { cloudEvents: List<Event> ->

                            deleteIfNotExist(cloudEvents)

                            cloudEvents.forEach { cloudEvent ->
                                updateLocalEventBasedOn(cloudEvent)
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
                localDatabase.deleteEvent(localEvent)
            }
        }
    }

    private fun updateLocalEventBasedOn(cloudEvent: Event) {
        localDatabase.updateLocalEventBasedOn(cloudEvent)
    }

    fun closeDatabase() {
        localDatabase.closeDatabase()
        if (::remoteListenerDisposable.isInitialized && !remoteListenerDisposable.isDisposed) {
            remoteListenerDisposable.dispose()
        }
    }

}