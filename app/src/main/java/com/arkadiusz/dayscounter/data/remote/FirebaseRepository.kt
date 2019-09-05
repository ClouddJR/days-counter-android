package com.arkadiusz.dayscounter.data.remote

import android.net.Uri
import com.arkadiusz.dayscounter.data.local.UserRepository
import com.arkadiusz.dayscounter.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Observable
import java.io.File

/**
 * Created by Arkadiusz on 14.03.2018
 */

class FirebaseRepository(
        private val userRepository: UserRepository = UserRepository(),
        private val firestoreDatabase: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    fun getNewId(): String {
        return if (userRepository.isLoggedIn()) {
            firestoreDatabase.collection(userRepository.getUserId()).document().id
        } else {
            firestoreDatabase.collection("newId").document().id
        }
    }


    fun getEvents(): Observable<List<Event>> {
        return Observable.create { emitter ->
            firestoreDatabase
                    .collection(userRepository.getUserId())
                    .addSnapshotListener { querySnapshot, exception ->
                        exception?.let {
                            emitter.onError(it)
                            return@addSnapshotListener
                        }

                        val tempList = mutableListOf<Event>()
                        querySnapshot?.forEach {
                            tempList.add(it.toObject(Event::class.java))
                        }

                        emitter.onNext(tempList)
                    }
        }
    }

    fun addEvent(event: Event) {
        val document = firestoreDatabase.collection(userRepository.getUserId())
                .document(event.id)

        val eventToBeAdded = HashMap<String, Any>()
        eventToBeAdded["id"] = event.id
        eventToBeAdded["name"] = event.name
        eventToBeAdded["date"] = event.date
        eventToBeAdded["description"] = event.description
        eventToBeAdded["image"] = event.image
        eventToBeAdded["imageCloudPath"] = event.imageCloudPath
        eventToBeAdded["imageID"] = event.imageID
        eventToBeAdded["imageColor"] = event.imageColor
        eventToBeAdded["type"] = event.type
        eventToBeAdded["repeat"] = event.repeat
        eventToBeAdded["formatYearsSelected"] = event.formatYearsSelected
        eventToBeAdded["formatMonthsSelected"] = event.formatMonthsSelected
        eventToBeAdded["formatWeeksSelected"] = event.formatWeeksSelected
        eventToBeAdded["formatDaysSelected"] = event.formatDaysSelected
        eventToBeAdded["lineDividerSelected"] = event.lineDividerSelected
        eventToBeAdded["counterFontSize"] = event.counterFontSize
        eventToBeAdded["titleFontSize"] = event.titleFontSize
        eventToBeAdded["fontType"] = event.fontType
        eventToBeAdded["fontColor"] = event.fontColor
        eventToBeAdded["pictureDim"] = event.pictureDim



        document.set(eventToBeAdded)

    }

    fun deleteEvent(event: Event?) {
        event?.let {
            firestoreDatabase.collection(userRepository.getUserId())
                    .document(event.id)
                    .delete()
        }
    }

    fun addImageForEvent(event: Event) {
        if (event.image.isNotEmpty() && event.image != "null" && File(event.image).exists()) {
            if (event.imageCloudPath.isEmpty()) {
                event.imageCloudPath = getCloudImagePath(event)
            }
            val storageReference = storage.getReference(event.imageCloudPath)
            storageReference.putFile(Uri.fromFile(File(event.image)))
        }
    }

    private fun getCloudImagePath(event: Event): String {
        val imageName = Uri.parse(event.image).lastPathSegment
        return "${userRepository.getUserId()}/${event.id}/$imageName"
    }

    fun deleteImageForEvent(oldEvent: Event?) {
        oldEvent?.let {
            if (it.imageCloudPath.isNotEmpty()) {
                val storageReference = storage.getReference(it.imageCloudPath)
                storageReference.delete()
            }
        }
    }
}