package com.arkadiusz.dayscounter.ui.widget

import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.RealmResults
import javax.inject.Inject

@HiltViewModel
class WidgetConfigureActivityViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    fun getAllEvents(): RealmResults<Event> {
        return databaseRepository.getAllEvents()
    }

    fun setWidgetIdForEvent(eventId: String, appWidgetId: Int) {
        databaseRepository.setWidgetIdForEvent(eventId, appWidgetId)
    }

    fun setWidgetTransparencyFor(eventId: String, isTransparent: Boolean) {
        databaseRepository.setWidgetTransparencyFor(eventId, isTransparent)
    }
}