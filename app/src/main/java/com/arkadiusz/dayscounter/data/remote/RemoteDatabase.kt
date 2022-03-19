package com.arkadiusz.dayscounter.data.remote

import android.net.Uri
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class RemoteDatabase @Inject constructor(
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    fun getNewId(): String {
        return if (userRepository.isLoggedIn()) {
            firestore.collection(userRepository.getUserId()).document().id
        } else {
            firestore.collection("newId").document().id
        }
    }

    fun getEvents(): Observable<List<Event>> {
        return Observable.create { emitter ->
            firestore
                .collection(userRepository.getUserId())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val tempList = mutableListOf<Event>()
                    querySnapshot?.forEach {
                        tempList.add(it.toObject(Event::class.java))
                    }

                    emitter.onNext(tempList)
                }
                .addOnFailureListener { exception ->
                    emitter.onError(exception)
                }
        }
    }

    fun addOrUpdateEvent(event: Event) {
        val document = firestore.collection(userRepository.getUserId())
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
            firestore.collection(userRepository.getUserId())
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