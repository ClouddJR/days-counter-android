package com.arkadiusz.dayscounter.fragments

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.content.Context.VIBRATOR_SERVICE
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
import com.arkadiusz.dayscounter.activities.DetailActivity
import com.arkadiusz.dayscounter.activities.EditActivity
import com.arkadiusz.dayscounter.adapters.EventsAdapter
import com.arkadiusz.dayscounter.adapters.RecyclerItemClickListener
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.repositories.FirebaseRepository
import com.arkadiusz.dayscounter.utils.RemindersUtils
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity

/**
 * Created by arkadiusz on 23.03.18
 */

class FutureFragment : Fragment() {

    private val databaseRepository = DatabaseRepository()
    private val firebaseRepository = FirebaseRepository()

    private var sortType = ""
    private lateinit var adapter: EventsAdapter
    private lateinit var recyclerView: RecyclerView
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
        initRecyclerView(view)
        setUpRecyclerView()
        hideFABOnScroll()
        return view
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnItemTouchListener(object : RecyclerItemClickListener(context!!, recyclerView, object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val id = eventsList[position].id
                startActivity<DetailActivity>("event_id" to id)
            }

            override fun onItemLongClick(view: View?, position: Int) {
                vibration()
                displayEventOptions(eventsList[position].id, eventsList[position])
            }

        }) {})
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
        eventsList = databaseRepository.getFutureEvents()
        sortEventsList()
    }

    private fun sortEventsList() {
        when (sortType) {
            "date_desc" -> eventsList = databaseRepository.sortEventsDateDesc(eventsList)
            "date_asc" -> eventsList = databaseRepository.sortEventsDateAsc(eventsList)
        }
    }

    private fun setUpRecyclerView() {
        adapter = EventsAdapter(context!!, eventsList)
        recyclerView.adapter = adapter
    }

    private fun vibration() {
        val vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(25)
        }
    }

    private fun displayEventOptions(eventId: Int, event: Event) {
        selector(getString(R.string.fragment_main_dialog_title), eventContextOptions, { _, i ->
            when (i) {
                0 -> startActivity<EditActivity>("eventId" to eventId)
                1 -> {
                    alert(getString(R.string.fragment_delete_dialog_question)) {
                        positiveButton(android.R.string.yes) {
                            RemindersUtils.deleteReminder(context!!, event)
                            databaseRepository.deleteEventFromDatabase(eventId)
                            firebaseRepository.deleteEvent(defaultPrefs(context!!)["firebase-email"]
                                    ?: "", eventId)
                        }
                        negativeButton(android.R.string.no) {}
                    }.show()
                }
            }
        })
    }

    private fun hideFABOnScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0) {
                    activity?.fab?.hide()
                } else if (dy < 0) {
                    activity?.fab?.show()
                }
            }
        })
    }

    fun refreshData() {
        receiveSortType()
        setUpData()
        setUpRecyclerView()
    }
}