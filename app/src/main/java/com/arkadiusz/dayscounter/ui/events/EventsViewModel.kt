package com.arkadiusz.dayscounter.ui.events

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.RemindersUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.RealmResults
import io.realm.Sort
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var _isCompactViewMode = MutableLiveData<Boolean>()
    val isCompactViewMode: LiveData<Boolean> = _isCompactViewMode

    private val eventsPastList = databaseRepository.getPastEvents().sortedByPastDate()
    private val eventsFutureList = databaseRepository.getFutureEvents().sortedByFutureDate()

    init {
        databaseRepository.syncToCloud()
        registerSharedPreferencesListener()
    }

    fun deleteEventAndRelatedReminder(context: Context, event: Event) {
        RemindersUtils.deleteReminder(context, event)
        databaseRepository.deleteEvent(event.id)
    }

    fun moveEventToFuture(event: Event) = databaseRepository.moveEventToFuture(event)

    fun moveEventToPast(event: Event) = databaseRepository.moveEventToPast(event)

    fun repeatEvent(event: Event) = databaseRepository.repeatEvent(event)

    fun saveCloudImageLocallyFrom(event: Event, context: Context) {
        val sourceDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        databaseRepository.saveCloudImageLocallyFrom(event, sourceDirectory!!)
    }

    fun getPastEvents(): RealmResults<Event> = eventsPastList

    fun getFutureEvents(): RealmResults<Event> = eventsFutureList

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "is_compact_view") {
            _isCompactViewMode.value = sharedPreferences?.getBoolean(key, false)
        }
    }

    private fun registerSharedPreferencesListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun RealmResults<Event>.sortedByPastDate(): RealmResults<Event> {
        return when (sharedPreferences["sort_type"] ?: "date_order") {
            "date_desc" -> sort("date", Sort.ASCENDING)
            "date_asc" -> sort("date", Sort.DESCENDING)
            else -> this
        }
    }

    private fun RealmResults<Event>.sortedByFutureDate(): RealmResults<Event> {
        return when (sharedPreferences["sort_type"] ?: "date_order") {
            "date_desc" -> sort("date", Sort.DESCENDING)
            "date_asc" -> sort("date", Sort.ASCENDING)
            else -> this
        }
    }
}