package com.arkadiusz.dayscounter.ui.events

import PreferenceUtils
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.repository.UserRepository
import com.arkadiusz.dayscounter.utils.RemindersUtils
import io.realm.RealmResults

class EventsViewModel(
        private val databaseRepository: DatabaseRepository = DatabaseRepository()
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var eventsPastList: RealmResults<Event>
    private lateinit var eventsFutureList: RealmResults<Event>

    var isCompactViewMode = MutableLiveData<Boolean>()

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    fun init(sortType: String, context: Context?) {
        if (!::eventsFutureList.isInitialized && !::eventsPastList.isInitialized) {

            databaseRepository.syncToCloud(context)

            eventsPastList = databaseRepository.getPastEvents()
            eventsFutureList = databaseRepository.getFutureEvents()

            sortEventsList(sortType)
        }
        registerSharedPreferencesListener(context)
    }

    private fun registerSharedPreferencesListener(context: Context?) {
        context?.let {
            PreferenceUtils.defaultPrefs(it).registerOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "is_compact_view") {
            isCompactViewMode.value = sharedPreferences?.getBoolean(key, false)
        }
    }

    fun fetchData(context: Context?) {
        databaseRepository.syncToCloud(context)
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
        databaseRepository.deleteEvent(event.id)
    }

    fun moveEventToFuture(event: Event) {
        databaseRepository.moveEventToFuture(event)
    }

    fun moveEventToPast(event: Event) {
        databaseRepository.moveEventToPast(event)
    }

    fun repeatEvent(event: Event) {
        databaseRepository.repeatEvent(event)
    }

    fun saveCloudImageLocallyFrom(event: Event, context: Context) {
        val sourceDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        databaseRepository.saveCloudImageLocallyFrom(event, sourceDirectory!!)
    }

    fun getPastEvents(): RealmResults<Event> = eventsPastList
    fun getFutureEvents(): RealmResults<Event> = eventsFutureList
}