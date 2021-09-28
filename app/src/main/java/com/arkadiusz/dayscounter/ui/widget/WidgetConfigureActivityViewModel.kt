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

    fun setWidgetIdForEvent(eventId: String, appWidgetId: Int) {
        databaseRepository.setWidgetIdForEvent(eventId, appWidgetId)
    }

    fun setWidgetTransparencyFor(eventId: String, isTransparent: Boolean) {
        databaseRepository.setWidgetTransparencyFor(eventId, isTransparent)
    }
}