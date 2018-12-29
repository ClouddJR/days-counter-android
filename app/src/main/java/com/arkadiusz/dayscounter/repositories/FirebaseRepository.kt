package com.arkadiusz.dayscounter.repositories

import android.content.Context
import android.net.ConnectivityManager
import com.arkadiusz.dayscounter.model.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by Arkadiusz on 14.03.2018
 */

class FirebaseRepository {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val databaseRepository = DatabaseProvider.provideRepository()

    private lateinit var refreshListener: RefreshListener

    interface RefreshListener {
        fun refreshFragments()
    }

    fun setRefreshListener(listener: RefreshListener) {
        refreshListener = listener
    }

    companion object {
        fun isNetworkEnabled(context: Context): Boolean {
            val connectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo != null && connectivityManager
                    .activeNetworkInfo.isConnected
        }
    }

    fun deletePreviousMail(mail: String) {
        databaseReference.child(mail.hashCode().toString()).removeValue()
    }

    fun deleteEvent(mail: String, id: Int) {
        if (mail.isNotEmpty()) {
            databaseReference.child(mail.hashCode().toString()).child(id.toString()).removeValue()
        }
    }

    fun processSyncOperationFor(selectedMail: String) {
        databaseReference.child(selectedMail.hashCode().toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(eventsList: DataSnapshot?) {
                val eventsTempList = mutableListOf<Event>()
                eventsList?.children?.let { it.mapTo(eventsTempList) { it.getValue(Event::class.java) as Event } }
                addLocalEventsToFirebase(eventsTempList, selectedMail)
            }
        })
    }

    private fun addLocalEventsToFirebase(firebaseEvents: MutableList<Event>, selectedMail: String) {
        var lastIdInFirebase = 0
        if (firebaseEvents.isNotEmpty()) {
            lastIdInFirebase = firebaseEvents.last().id
        }

        val localEvents = databaseRepository.getAllEvents()

        if (selectedMail.isNotEmpty()) {
            for (localEvent in localEvents) {
                if (isEventUnique(localEvent, firebaseEvents)) {
                    addOrEditEventInFirebase(selectedMail, localEvent, ++lastIdInFirebase)
                }
            }
            databaseRepository.deleteAllEventsFromDatabase()
            getAllEventsFor(selectedMail)
        }
    }

    private fun isEventUnique(event: Event, eventsList: MutableList<Event>): Boolean {
        var isUnique = true
        for (eventInFirebase in eventsList) {
            if (eventInFirebase.name == event.name &&
                    eventInFirebase.date == event.date &&
                    eventInFirebase.description == event.description &&
                    eventInFirebase.repeat == event.repeat &&
                    eventInFirebase.image == event.image &&
                    eventInFirebase.imageID == event.imageID &&
                    eventInFirebase.imageColor == event.imageColor &&
                    eventInFirebase.fontColor == event.fontColor &&
                    eventInFirebase.titleFontSize == event.titleFontSize &&
                    eventInFirebase.counterFontSize == event.counterFontSize &&
                    eventInFirebase.pictureDim == event.pictureDim) {
                isUnique = false
            }
        }

        return isUnique
    }

    fun addOrEditEventInFirebase(userMail: String, event: Event, id: Int) {
        if (userMail.isNotEmpty()) {
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("id").setValue(id)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("name").setValue(event.name)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("date").setValue(event.date)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("description").setValue(event.description)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("image").setValue(event.image)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("imageID").setValue(event.imageID)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("imageColor").setValue(event.imageColor)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("type").setValue(event.type)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("repeat").setValue(event.repeat)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("widgetID").setValue(event.widgetID)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("hasAlarm").setValue(event.hasAlarm)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("hasTransparentWidget").setValue(event.hasTransparentWidget)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("reminderYear").setValue(event.reminderYear)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("reminderMonth").setValue(event.reminderMonth)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("reminderDay").setValue(event.reminderDay)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("reminderHour").setValue(event.reminderHour)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("reminderMinute").setValue(event.reminderMinute)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("notificationText").setValue(event.notificationText)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("formatYearsSelected").setValue(event.formatYearsSelected)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("formatMonthsSelected").setValue(event.formatMonthsSelected)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("formatWeeksSelected").setValue(event.formatWeeksSelected)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("formatDaysSelected").setValue(event.formatDaysSelected)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("lineDividerSelected").setValue(event.lineDividerSelected)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("counterFontSize").setValue(event.counterFontSize)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("titleFontSize").setValue(event.titleFontSize)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("fontType").setValue(event.fontType)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("fontColor").setValue(event.fontColor)
            databaseReference.child(userMail.hashCode().toString()).child(id.toString()).child("pictureDim").setValue(event.pictureDim)
        }
    }

    private fun getAllEventsFor(mail: String) {
        databaseReference.child(mail.hashCode().toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(eventsList: DataSnapshot?) {
                val eventsTempList = mutableListOf<Event>()
                eventsList?.children?.let { it.mapTo(eventsTempList) { it.getValue(Event::class.java) as Event } }
                databaseRepository.addEventsToDatabase(eventsTempList)
                refreshListener.refreshFragments()
            }
        })
    }


}