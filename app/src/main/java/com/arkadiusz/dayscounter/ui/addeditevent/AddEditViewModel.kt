package com.arkadiusz.dayscounter.ui.addeditevent

import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository

class AddEditViewModel(
        private val databaseRepository: DatabaseRepository = DatabaseRepository()
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    fun addEvent(eventToBeAdded: Event) {
        databaseRepository.addEvent(eventToBeAdded)
    }

    fun editEvent(eventToBeEdited: Event) {
        databaseRepository.editEvent(eventToBeEdited)
    }

    fun getPassedEventById(eventId: String): Event {
        return databaseRepository.getEventById(eventId)!!
    }
}