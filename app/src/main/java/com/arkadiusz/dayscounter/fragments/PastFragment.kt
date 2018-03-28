package com.arkadiusz.dayscounter.fragments

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.activities.AddActivity
import com.arkadiusz.dayscounter.activities.DetailActivity
import com.arkadiusz.dayscounter.adapters.EventsAdapter
import com.arkadiusz.dayscounter.database.Event
import com.arkadiusz.dayscounter.model.RecyclerItemClickListener
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import io.realm.RealmResults
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity

/**
 * Created by arkadiusz on 24.03.18
 */

class PastFragment : Fragment() {

    private val databaseRepository = DatabaseRepository()

    private var sortType = ""
    private lateinit var eventsList: RealmResults<Event>
    private lateinit var eventContextOptions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiveSortType()
        setUpContextOptions()
        setUpData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.future_fragment, container, false)
        setUpRecyclerView(view)
        return view
    }

    private fun receiveSortType() {
        context?.let {
            val sharedPref = defaultPrefs(it)
            sortType = sharedPref["sort_type"] ?: "date_order"
        }
    }

    private fun setUpContextOptions() {
        eventContextOptions = listOf(getString(R.string.fragment_main_dialog_option_edit),
                getString(R.string.fragment_main_dialog_option_delete))
    }

    private fun setUpData() {
        eventsList = databaseRepository.getPastEvents()
        sortEventsList()
    }

    private fun sortEventsList() {
        when (sortType) {
            "date_desc" -> eventsList = databaseRepository.sortEventsDateDesc(eventsList)
            "date_asc" -> eventsList = databaseRepository.sortEventsDateAsc(eventsList)
        }
    }

    private fun setUpRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = EventsAdapter(context!!, eventsList)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addOnItemTouchListener(object : RecyclerItemClickListener(context!!, recyclerView, object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val id = eventsList[position].id
                startActivity<DetailActivity>("event_id" to id)
            }

            override fun onItemLongClick(view: View?, position: Int) {
                vibration()
                displayEventOptions(eventsList[position].id)
            }

        }) {})
    }

    private fun vibration() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(25)
        }
    }

    private fun displayEventOptions(eventId: Int) {
        selector(getString(R.string.fragment_main_dialog_title), eventContextOptions, { _, i ->
            when (i) {
                0 -> startActivity<AddActivity>("Event Type" to "past")
                1 -> {
                    alert(getString(R.string.fragment_delete_dialog_question)) {
                        positiveButton(android.R.string.yes) {
                            databaseRepository.deleteEventFromDatabase(eventId)
                        }
                        negativeButton(android.R.string.no) {}
                    }.show()
                }
            }
        })
    }

    fun refreshData() {
        receiveSortType()
        setUpData()
    }
}