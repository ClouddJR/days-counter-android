package com.arkadiusz.dayscounter.ui.events

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.ui.addeditevent.EditActivity
import com.arkadiusz.dayscounter.ui.common.RecyclerItemClickListener
import com.arkadiusz.dayscounter.ui.eventdetails.DetailActivity
import com.arkadiusz.dayscounter.util.ExtensionUtils.getViewModel
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

class PastEventsFragment : Fragment() {

    private lateinit var viewModel: EventsViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsList: RealmResults<Event>
    private lateinit var eventContextOptions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSharedPreferences()
        initViewModel()
        setUpData()
        setUpContextOptions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.past_fragment_xml, container, false)
        observeState()
        initRecyclerView(view)
        setUpRecyclerViewData(sharedPreferences["is_compact_view", false] ?: false)
        if (savedInstanceState == null) scheduleRVAnimation()
        addOnScrollListener()
        return view
    }

    private fun initSharedPreferences() {
        sharedPreferences = defaultPrefs(requireContext())
    }

    private fun initViewModel() {
        viewModel = getViewModel(requireActivity())

        val sortType = sharedPreferences["sort_type"] ?: "date_order"

        viewModel.init(sortType, context)
    }

    private fun setUpData() {
        eventsList = viewModel.getPastEvents()
    }

    private fun setUpContextOptions() {
        eventContextOptions = listOf(
            getString(R.string.fragment_main_dialog_option_edit),
            getString(R.string.fragment_main_dialog_option_delete)
        )
    }

    private fun observeState() {
        viewModel.isCompactViewMode.observe(viewLifecycleOwner, Observer {
            setUpRecyclerViewData(it)
            scheduleRVAnimation()
        })
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnItemTouchListener(object :
            RecyclerItemClickListener(requireContext(), recyclerView, object : OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    val id = eventsList[position]!!.id
                    context?.startActivity<DetailActivity>("event_id" to id)
                }

                override fun onItemLongClick(view: View?, position: Int) {
                    vibration()
                    displayEventOptions(eventsList[position]!!)
                }

            }) {})
    }

    private fun setUpRecyclerViewData(isCompactView: Boolean) {
        val adapter = EventsAdapter(requireContext(), isCompactView, eventsList,
            object : EventsAdapter.Delegate {
                override fun moveEventToFuture(event: Event) {
                    viewModel.moveEventToFuture(event)
                }

                override fun moveEventToPast(event: Event) {
                    viewModel.moveEventToPast(event)
                }

                override fun repeatEvent(event: Event) {
                    viewModel.repeatEvent(event)
                }

                override fun saveCloudImageLocallyFrom(event: Event, context: Context) {
                    viewModel.saveCloudImageLocallyFrom(event, context)
                }
            })
        recyclerView.adapter = adapter
    }

    private fun scheduleRVAnimation() {
        recyclerView.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_bottom_top)
        recyclerView.scheduleLayoutAnimation()
    }

    private fun vibration() {
        val vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(25)
        }
    }

    private fun displayEventOptions(event: Event) {
        context?.let { ctx ->
            ctx.selector(
                getString(R.string.fragment_main_dialog_title),
                eventContextOptions
            ) { _, i ->
                when (i) {
                    0 -> ctx.startActivity<EditActivity>("eventId" to event.id)
                    1 -> {
                        ctx.alert(getString(R.string.fragment_delete_dialog_question)) {
                            positiveButton(android.R.string.yes) {
                                viewModel.deleteEventAndRelatedReminder(requireContext(), event)
                            }
                            negativeButton(android.R.string.no) {}
                        }.show()
                    }
                }
            }
        }
    }

    private fun addOnScrollListener() {
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
}