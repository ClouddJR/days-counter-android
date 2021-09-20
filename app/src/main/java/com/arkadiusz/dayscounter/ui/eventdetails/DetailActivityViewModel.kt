package com.arkadiusz.dayscounter.ui.eventdetails

import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.model.Event

class DetailActivityViewModel(
    private val databaseRepository: DatabaseRepository = DatabaseRepository()
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    fun getEventById(eventId: String): Event? {
        return databaseRepository.getEventById(eventId)
    }

    fun deleteEvent(eventId: String) {
        databaseRepository.deleteEvent(eventId)
    }
}