package com.arkadiusz.dayscounter.ui.events

import com.arkadiusz.dayscounter.data.model.Event
import io.realm.RealmResults

class FutureEventsFragment : EventsFragment() {
    override fun getData(): RealmResults<Event> = viewModel.getFutureEvents()
}