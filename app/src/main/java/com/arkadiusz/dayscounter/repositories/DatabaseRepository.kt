package com.arkadiusz.dayscounter.repositories

import android.content.Context
import com.arkadiusz.dayscounter.database.Event
import com.arkadiusz.dayscounter.model.Migration
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DatabaseRepository {

    private var realm: Realm

    object RealmInitializer {
        fun initRealm(context: Context) {
            Realm.init(context)
        }
    }

    init {
        val config = RealmConfiguration.Builder()
                .schemaVersion(3)
                .migration(Migration())
                .build()
        realm = Realm.getInstance(config)
    }

    fun addEventToDatabase(event: Event) {
        event.id = getNextId()
        realm.executeTransaction {
            it.copyToRealmOrUpdate(event)
        }
        updateViewModel()
    }

    private fun getNextId(): Int {
        var nextId = 1
        try {
            nextId = realm.where(Event::class.java).max("id").toInt() + 1
        } catch (e: NullPointerException) {
            return nextId
        }
        return nextId
    }

    private fun updateViewModel() {

    }

    fun deleteEventFromDatabase(eventId: Int) {
        val results = realm.where(Event::class.java).equalTo("id", eventId).findAll()
        val eventToBeDeleted = results.first()
        realm.executeTransaction {
            results.deleteAllFromRealm()
        }
    }


}