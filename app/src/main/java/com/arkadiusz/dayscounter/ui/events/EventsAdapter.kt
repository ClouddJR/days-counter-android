package com.arkadiusz.dayscounter.ui.events

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.util.DateUtils
import com.arkadiusz.dayscounter.util.DateUtils.calculateDate
import com.arkadiusz.dayscounter.util.DateUtils.generateCalendar
import com.arkadiusz.dayscounter.util.DateUtils.generateTodayCalendar
import com.arkadiusz.dayscounter.util.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.util.FontUtils.getFontFor
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.event_compact_counter_stack.view.*
import kotlinx.android.synthetic.main.single_event_layout.view.*
import kotlinx.android.synthetic.main.single_event_layout_compact.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.io.File
import java.util.*

class EventsAdapter(
    private val context: Context,
    private val isCompactView: Boolean,
    private val viewModel: EventsViewModel,
    private val eventsList: OrderedRealmCollection<Event>,
) : RealmRecyclerViewAdapter<Event, EventsAdapter.ViewHolder>(eventsList, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        return ViewHolder(view)
    }

    private fun getLayoutId(): Int {
        return if (isCompactView) R.layout.single_event_layout_compact else R.layout.single_event_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(event: Event) {
            if (isCompactView) {
                displayTitle(event, view.eventTitleTextView)
                displayCompactCounterText(event)
                displayDate(event)
                displayImage(event, view.eventImageView)
            } else {
                displayTitle(event, view.eventTitle)
                displayCounterText(event)
                displayImage(event, view.eventImage)
                hideOrShowDivider(event)
                changeFonts(event)
                dimPicture(event)
            }
            repeatIfNecessary(event)
        }

        private fun displayCounterText(event: Event) {
            val counterText = calculateDate(
                event.date,
                event.formatYearsSelected,
                event.formatMonthsSelected,
                event.formatWeeksSelected,
                event.formatDaysSelected,
                context.resources
            )
            view.eventCalculateText.text = counterText
        }

        private fun displayCompactCounterText(event: Event) {
            val dateElements = getElementsFromDate(event.date)
            val calculatedComponents = calculateDate(
                dateElements.first,
                dateElements.second,
                dateElements.third,
                event.formatYearsSelected,
                event.formatMonthsSelected,
                event.formatWeeksSelected,
                event.formatDaysSelected
            )

            view.counterStackView.yearsSection.visibility = View.GONE
            view.counterStackView.monthsSection.visibility = View.GONE
            view.counterStackView.weeksSection.visibility = View.GONE
            view.counterStackView.daysSection.visibility = View.GONE

            if (calculatedComponents.years == 0 && calculatedComponents.months == 0 &&
                calculatedComponents.weeks == 0 && calculatedComponents.days == 0
            ) {
                view.counterStackView.daysSection.visibility = View.VISIBLE
                view.counterStackView.daysCaptionTextView.visibility = View.GONE
                view.counterStackView.daysNumberTextView.text = context
                    .getString(R.string.date_utils_today)
            } else {
                if (event.formatYearsSelected) {
                    view.counterStackView.yearsSection.visibility = View.VISIBLE
                    view.counterStackView.yearsCaptionTextView.text = context
                        .resources.getQuantityText(
                            R.plurals.years_number,
                            calculatedComponents.years
                        )
                    view.counterStackView.yearsNumberTextView.text =
                        calculatedComponents.years.toString()
                }

                if (event.formatMonthsSelected) {
                    view.counterStackView.monthsSection.visibility = View.VISIBLE
                    view.counterStackView.monthsCaptionTextView.text = context
                        .resources.getQuantityText(
                            R.plurals.months_number,
                            calculatedComponents.months
                        )
                    view.counterStackView.monthsNumberTextView.text =
                        calculatedComponents.months.toString()
                }

                if (event.formatWeeksSelected) {
                    view.counterStackView.weeksSection.visibility = View.VISIBLE
                    view.counterStackView.weeksCaptionTextView.text = context
                        .resources.getQuantityText(
                            R.plurals.weeks_number,
                            calculatedComponents.weeks
                        )
                    view.counterStackView.weeksNumberTextView.text =
                        calculatedComponents.weeks.toString()
                }

                if (event.formatDaysSelected) {
                    view.counterStackView.daysSection.visibility = View.VISIBLE
                    view.counterStackView.daysCaptionTextView.visibility = View.VISIBLE
                    view.counterStackView.daysCaptionTextView.text = context
                        .resources.getQuantityText(
                            R.plurals.days_number,
                            calculatedComponents.days
                        )
                    view.counterStackView.daysNumberTextView.text =
                        calculatedComponents.days.toString()
                }
            }

        }

        private fun displayDate(event: Event) {
            val formattedDate = DateUtils.formatDateAccordingToSettings(
                event.date, defaultPrefs(context)["dateFormat"]
                    ?: ""
            )
            view.eventDateTextView.text = formattedDate
        }

        private fun displayTitle(event: Event, textView: TextView) {
            textView.text = event.name
        }

        private fun displayImage(event: Event, imageView: ImageView) {
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.setColorSchemeColors(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                )
            )
            circularProgressDrawable.start()

            when {
                event.imageColor != 0 -> {
                    imageView.setImageDrawable(null)
                    imageView.backgroundColor = event.imageColor
                }
                event.imageID == 0 -> {
                    when {
                        File(event.image).exists() ->
                            Glide.with(context)
                                .load(event.image)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(imageView)
                        event.imageCloudPath.isNotEmpty() -> {
                            Glide.with(context)
                                .load(
                                    FirebaseStorage.getInstance().getReference(event.imageCloudPath)
                                )
                                .placeholder(circularProgressDrawable)
                                .into(imageView)
                            viewModel.saveCloudImageLocallyFrom(event, context)
                        }
                        else ->
                            Glide.with(context).load(android.R.color.darker_gray)
                                .into(imageView)
                    }
                }
                else -> Glide.with(context).load(event.imageID).into(imageView)
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
            view.eventCalculateText.setTextSize(
                TypedValue.COMPLEX_UNIT_DIP,
                event.counterFontSize.toFloat()
            )
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
                            viewModel.moveEventToPast(event)
                        } else {
                            viewModel.repeatEvent(event)
                        }
                    }
                }
                "past" -> {
                    if (eventDateIsFromTheFuture(event)) {
                        viewModel.moveEventToFuture(event)
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

            return eventCalendar.before(todayCalendar) &&
                    todayCalendar.get(Calendar.DAY_OF_MONTH) != day
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

            return todayCalendar.before(eventCalendar) &&
                    todayCalendar.get(Calendar.DAY_OF_MONTH) != day
        }
    }
}