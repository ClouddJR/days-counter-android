package com.arkadiusz.dayscounter.ui.widget

import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import io.realm.RealmResults

class WidgetConfigureActivityViewModel(
        private val databaseRepository: DatabaseRepository = DatabaseRepository()
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    fun getAllEvents(): RealmResults<Event> {
        return databaseRepository.getAllEvents()
    }

    fun getEventById(eventId: String): Event? {
        return databaseRepository.getEventById(eventId)
    }

    fun setWidgetIdForEvent(event: Event, appWidgetId: Int) {
        databaseRepository.setWidgetIdForEvent(event, appWidgetId)
    }

    fun setWidgetTransparencyFor(event: Event, isTransparent: Boolean) {
        databaseRepository.setWidgetTransparencyFor(event, isTransparent)
    }

}