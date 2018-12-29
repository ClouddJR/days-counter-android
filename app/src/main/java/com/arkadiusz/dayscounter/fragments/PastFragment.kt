package com.arkadiusz.dayscounter.fragments

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.activities.DetailActivity
import com.arkadiusz.dayscounter.activities.EditActivity
import com.arkadiusz.dayscounter.adapters.EventsAdapter
import com.arkadiusz.dayscounter.adapters.RecyclerItemClickListener
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.repositories.DatabaseProvider
import com.arkadiusz.dayscounter.repositories.FirebaseRepository
import com.arkadiusz.dayscounter.utils.RemindersUtils.deleteReminder
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

/**
 * Created by arkadiusz on 24.03.18
 */

class PastFragment : Fragment() {

    private val databaseRepository = DatabaseProvider.provideRepository()
    private val firebaseRepository = FirebaseRepository()

    private var sortType = ""
    private lateinit var adapter: EventsAdapter
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
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
                context?.startActivity<DetailActivity>("event_id" to id)
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
        eventsList = databaseRepository.getPastEvents()
        sortEventsList()
    }

    private fun sortEventsList() {
        when (sortType) {
            "date_desc" -> eventsList = databaseRepository.sortEventsDateAsc(eventsList)
            "date_asc" -> eventsList = databaseRepository.sortEventsDateDesc(eventsList)
        }
    }

    private fun setUpRecyclerView() {
        adapter = EventsAdapter(context!!, eventsList)
        recyclerView.adapter = adapter
        recyclerView.scheduleLayoutAnimation()
        recyclerView.invalidate()
    }

    private fun vibration() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(25)
        }
    }

    private fun displayEventOptions(eventId: Int, event: Event) {
        context?.let { ctx ->
            ctx.selector(getString(R.string.fragment_main_dialog_title), eventContextOptions) { _, i ->
                when (i) {
                    0 -> ctx.startActivity<EditActivity>("eventId" to eventId)
                    1 -> {
                        ctx.alert(getString(R.string.fragment_delete_dialog_question)) {
                            positiveButton(android.R.string.yes) {
                                deleteReminder(context!!, event)
                                databaseRepository.deleteEventFromDatabase(eventId)
                                firebaseRepository.deleteEvent(defaultPrefs(context!!)["firebase-email"]
                                        ?: "", eventId)
                            }
                            negativeButton(android.R.string.no) {}
                        }.show()
                    }
                }
            }
        }
    }

    private fun hideFABOnScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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