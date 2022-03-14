package com.arkadiusz.dayscounter.ui.events

import android.content.SharedPreferences
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.ui.addeditevent.EditActivity
import com.arkadiusz.dayscounter.ui.eventdetails.DetailActivity
import com.arkadiusz.dayscounter.util.PreferenceUtils
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import dagger.hilt.android.AndroidEntryPoint
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.events_fragment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

@AndroidEntryPoint
abstract class EventsFragment : Fragment(R.layout.events_fragment) {

    protected val viewModel: EventsViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var eventsList: RealmResults<Event>

    abstract fun getData(): RealmResults<Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSharedPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeState()
        initRecyclerView()
        setUpRecyclerViewData(sharedPreferences["is_compact_view", false] ?: false)
        if (savedInstanceState == null) {
            scheduleRVAnimation()
        }
        addOnScrollListener()
    }

    private fun initSharedPreferences() {
        sharedPreferences = PreferenceUtils.defaultPrefs(requireContext())
    }

    private fun observeState() {
        viewModel.isCompactViewMode.observe(viewLifecycleOwner) {
            setUpRecyclerViewData(it)
            scheduleRVAnimation()
        }
    }

    private fun initRecyclerView() {
        recyclerView.setHasFixedSize(true)
    }

    private fun setUpRecyclerViewData(isCompactView: Boolean) {
        val adapter = EventsAdapter(requireContext(), isCompactView, viewModel, getData(),
            onClick = { event -> context?.startActivity<DetailActivity>("event_id" to event.id) },
            onLongClick = { event -> displayEventOptions(event) }
        )
        recyclerView.adapter = adapter
    }

    private fun scheduleRVAnimation() {
        recyclerView.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_bottom_top)
        recyclerView.scheduleLayoutAnimation()
    }

    private fun vibration() {
        requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    private fun displayEventOptions(event: Event) {
        context?.let { ctx ->
            ctx.selector(
                getString(R.string.fragment_main_dialog_title),
                listOf(
                    getString(R.string.fragment_main_dialog_option_edit),
                    getString(R.string.fragment_main_dialog_option_delete)
                )
            ) { _, i ->
                when (i) {
                    0 -> ctx.startActivity<EditActivity>("eventId" to event.id)
                    1 -> {
                        ctx.alert(getString(R.string.fragment_delete_dialog_question)) {
                            positiveButton(android.R.string.ok) {
                                viewModel.deleteEventAndRelatedReminder(requireContext(), event)
                            }
                            negativeButton(android.R.string.cancel) {}
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