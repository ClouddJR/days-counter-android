package com.arkadiusz.dayscounter.ui.eventdetails

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.ui.addeditevent.EditActivity
import com.arkadiusz.dayscounter.util.DateUtils.calculateDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.util.DateUtils.formatTime
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.ThemeUtils
import com.arkadiusz.dayscounter.util.ViewModelUtils.getViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdRequest
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.MaterialColors
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_detail.*
import org.jetbrains.anko.*
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var viewModel: DetailActivityViewModel

    private var passedEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        setContentView(R.layout.activity_detail)

        initViewModel()
        fetchPassedEvent()

        if (passedEvent != null) {
            setStatusBarColor()
            setUpToolbar()
            cancelNotification()
            displayImage()
            fillMainSection()
            fillAboutSection()
            fillReminderSection()
            fillRepetitionSection()
            displayAd()
        } else {
            displayToastAndFinishActivity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                startActivity<EditActivity>("eventId" to passedEvent!!.id)
                finish()
            }

            R.id.action_delete -> {
                alert(getString(R.string.fragment_delete_dialog_question)) {
                    positiveButton(android.R.string.ok) {
                        viewModel.deleteEvent(passedEvent!!.id)
                        finish()
                    }
                    negativeButton(android.R.string.cancel) {}
                }.show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initViewModel() {
        viewModel = getViewModel(this)
    }

    private fun fetchPassedEvent() {
        passedEvent = viewModel.getEventById(intent.getStringExtra("event_id")!!)
    }

    private fun displayToastAndFinishActivity() {
        toast(getString(R.string.detail_activity_toast_event_deleted))
        finish()
    }

    private fun setStatusBarColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.half_translucent)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""

        fun isCollapsed(verticalOffset: Int): Boolean {
            return collapsingToolbar.height + verticalOffset <
                    collapsingToolbar.scrimVisibleHeightTrigger
        }

        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                if (isCollapsed(verticalOffset)) {
                    val iconColor = MaterialColors.getColor(toolbar, R.attr.colorOnSurface)
                    toolbar.setNavigationIconTint(iconColor)
                    toolbar.overflowIcon?.setTint(iconColor)
                } else {
                    toolbar.setNavigationIconTint(Color.WHITE)
                    toolbar.overflowIcon?.setTint(Color.WHITE)
                }
            })
    }

    private fun cancelNotification() {
        val isComingFromNotification = intent.getStringExtra("notificationClick")

        if (isComingFromNotification != null && isComingFromNotification == "clicked") {
            notificationManager.cancel(passedEvent!!.id.hashCode())
        }
    }

    private fun displayImage() {
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorAccent
            )
        )
        circularProgressDrawable.start()

        when {
            passedEvent!!.imageColor != 0 -> {
                eventImage.setImageDrawable(null)
                eventImage.backgroundColor = passedEvent!!.imageColor
            }
            passedEvent!!.imageID == 0 -> {
                when {
                    File(passedEvent!!.image).exists() ->
                        Glide.with(this)
                            .load(passedEvent!!.image)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(eventImage)
                    passedEvent!!.imageCloudPath.isNotEmpty() -> {
                        Glide.with(this)
                            .load(
                                FirebaseStorage.getInstance()
                                    .getReference(passedEvent!!.imageCloudPath)
                            )
                            .placeholder(circularProgressDrawable)
                            .into(eventImage)
                    }
                    else -> {

                    }
                }
            }
            else -> Glide.with(this).load(passedEvent!!.imageID).into(eventImage)
        }
    }

    private fun fillMainSection() {
        val counterText = calculateDate(
            passedEvent!!.date,
            passedEvent!!.formatYearsSelected,
            passedEvent!!.formatMonthsSelected,
            passedEvent!!.formatWeeksSelected,
            passedEvent!!.formatDaysSelected, this
        )
        eventCalculateText.text = counterText
        eventTitle.text = passedEvent!!.name
    }

    private fun fillAboutSection() {
        dateText.text = formatDateAccordingToSettings(
            passedEvent!!.date,
            defaultPrefs(this)["dateFormat"] ?: ""
        )
        if (passedEvent!!.description.isNotEmpty()) {
            descriptionText.text = passedEvent!!.description
        } else {
            descriptionText.visibility = View.GONE
        }
    }

    private fun fillRepetitionSection() {
        repeatSectionText.text = when (passedEvent!!.repeat) {
            "0" -> getString(R.string.detail_once)
            "1" -> getString(R.string.detail_daily)
            "2" -> getString(R.string.detail_weekly)
            "3" -> getString(R.string.detail_monthly)
            "4" -> getString(R.string.detail_yearly)
            else -> ""
        }
    }

    private fun fillReminderSection() {
        if (passedEvent!!.reminderYear != 0) {
            val reminderDate = "${
                formatDateAccordingToSettings(
                    formatDate(
                        passedEvent!!.reminderYear,
                        passedEvent!!.reminderMonth,
                        passedEvent!!.reminderDay
                    ),
                    defaultPrefs(this)["dateFormat"] ?: ""
                )
            } " +
                    formatTime(passedEvent!!.reminderHour, passedEvent!!.reminderMinute)

            reminderSectionText.text = reminderDate
            reminderDescriptionText.text = passedEvent!!.notificationText
        } else {
            reminderDescriptionText.visibility = View.GONE
        }
    }


    private fun displayAd() {
        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean? = prefs["ads", false]
        if (areAdsRemoved != true) {
            adView.loadAd(AdRequest.Builder().build())
        } else {
            adView.visibility = View.GONE
            nestedScroll.setPadding(0, 0, 0, 0)
        }
    }
}