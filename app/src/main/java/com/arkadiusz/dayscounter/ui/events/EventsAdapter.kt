package com.arkadiusz.dayscounter.ui.events

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.utils.DateUtils.calculateDate
import com.arkadiusz.dayscounter.utils.DateUtils.generateCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.generateTodayCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.utils.FontUtils.getFontFor
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.single_event_layout.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.io.File

/**
 * Created by arkadiusz on 17.03.18
 */

class EventsAdapter(var context: Context, private var eventsList: OrderedRealmCollection<Event>) :
        RealmRecyclerViewAdapter<Event, EventsAdapter.ViewHolder>(eventsList, true) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_event_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    inner class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private val databaseRepository = DatabaseRepository()

        fun bind(event: Event) {
            displayCounterText(event)
            displayTitle(event)
            displayImage(event)
            hideOrShowDivider(event)
            changeFonts(event)
            dimPicture(event)
            repeatIfNecessary(event)
        }

        private fun displayCounterText(event: Event) {
            val counterText = calculateDate(event.date,
                    event.formatYearsSelected,
                    event.formatMonthsSelected,
                    event.formatWeeksSelected,
                    event.formatDaysSelected, context)
            view.eventCalculateText.text = counterText
        }

        private fun displayTitle(event: Event) {
            view.eventTitle.text = event.name
        }

        private fun displayImage(event: Event) {
            when {
                event.imageColor != 0 -> {
                    view.eventImage.setImageDrawable(null)
                    view.eventImage.backgroundColor = event.imageColor
                }
                event.imageID == 0 -> {
                    when {
                        File(event.image).exists() -> Glide.with(context).load(event.image).into(view.eventImage)
                        event.imageCloudPath.isNotEmpty() -> Glide.with(context).load(
                                FirebaseStorage.getInstance().getReference(event.imageCloudPath))
                                .into(view.eventImage)
                        else -> Glide.with(context).load(android.R.color.darker_gray)
                                .into(view.eventImage)
                    }
                }
                else -> Glide.with(context).load(event.imageID).into(view.eventImage)
            }
        }

        private fun hideOrShowDivider(event: Event) {
            if (!event.lineDividerSelected) {
                view.eventLine.visibility = View.GONE
            } else {
                view.eventLine.visibility = View.VISIBLE
            }
        }

        private fun changeFonts(event: Event) {
            view.eventTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, event.titleFontSize.toFloat())
            view.eventCalculateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, event.counterFontSize.toFloat())
            view.eventTitle.typeface = getFontFor(event.fontType, context)
            view.eventCalculateText.typeface = getFontFor(event.fontType, context)
            view.eventTitle.textColor = event.fontColor
            view.eventCalculateText.textColor = event.fontColor
            view.eventLine.backgroundColor = event.fontColor
        }

        private fun dimPicture(event: Event) {
            view.eventImage.setColorFilter(Color.argb(255 / 17 * event.pictureDim, 0, 0, 0))
        }

        private fun repeatIfNecessary(event: Event) {
            when (event.type) {
                "future" -> {
                    if (eventDateIsFromThePast(event)) {
                        if (eventIsNotRepeated(event)) {
                            databaseRepository.moveEventToPast(event)
                        } else {
                            databaseRepository.repeatEvent(event)
                        }
                    }
                }
                "past" -> {
                    if (eventDateIsFromTheFuture(event)) {
                        databaseRepository.moveEventToFuture(event)
                    }
                }
            }
        }

        private fun eventDateIsFromThePast(event: Event): Boolean {
            val todayCalendar = generateTodayCalendar()

            val eventTriple = getElementsFromDate(event.date)
            val year = eventTriple.first
            val month = eventTriple.second
            val day = eventTriple.third

            val eventCalendar = generateCalendar(year, month, day)

            return eventCalendar.before(todayCalendar)
        }

        private fun eventIsNotRepeated(event: Event): Boolean {
            return event.repeat == "0"
        }

        private fun eventDateIsFromTheFuture(event: Event): Boolean {
            val todayCalendar = generateTodayCalendar()

            val eventTriple = getElementsFromDate(event.date)
            val year = eventTriple.first
            val month = eventTriple.second
            val day = eventTriple.third

            val eventCalendar = generateCalendar(year, month, day)

            return todayCalendar.before(eventCalendar)
        }
    }
}