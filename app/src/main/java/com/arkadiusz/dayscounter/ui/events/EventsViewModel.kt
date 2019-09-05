package com.arkadiusz.dayscounter.ui.events

import android.content.Context
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.local.DatabaseProvider
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.utils.RemindersUtils
import io.realm.RealmResults

class EventsViewModel : ViewModel() {

    private val databaseRepository = DatabaseProvider.provideRepository()

    private lateinit var eventsPastList: RealmResults<Event>
    private lateinit var eventsFutureList: RealmResults<Event>

    fun init(sortType: String, context: Context?) {
        if (!::eventsFutureList.isInitialized && !::eventsPastList.isInitialized) {

            databaseRepository.syncToCloud(context)

            eventsPastList = databaseRepository.getPastEvents()
            eventsFutureList = databaseRepository.getFutureEvents()

            sortEventsList(sortType)
        }
    }

    private fun sortEventsList(sortType: String) {
        when (sortType) {
            "date_desc" -> {
                eventsPastList = databaseRepository.sortEventsDateAsc(eventsPastList)
                eventsFutureList = databaseRepository.sortEventsDateDesc(eventsFutureList)
            }
            "date_asc" -> {
                eventsPastList = databaseRepository.sortEventsDateDesc(eventsPastList)
                eventsFutureList = databaseRepository.sortEventsDateAsc(eventsFutureList)
            }
        }
    }

    fun deleteEventAndRelatedReminder(context: Context, event: Event) {
        RemindersUtils.deleteReminder(context, event)
        databaseRepository.deleteEventFromDatabase(event.id)
    }

    fun getPastEvents(): RealmResults<Event> = eventsPastList
    fun getFutureEvents(): RealmResults<Event> = eventsFutureList
}