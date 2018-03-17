package com.arkadiusz.dayscounter

import android.app.Application
import com.arkadiusz.dayscounter.repositories.DatabaseRepository

/**
 * Created by Arkadiusz on 14.03.2018
 */

class DaysCounterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DatabaseRepository.RealmInitializer.initRealm(this)
    }
}
