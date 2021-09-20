package com.arkadiusz.dayscounter.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.util.ExtensionUtils.getViewModel
import com.arkadiusz.dayscounter.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_app_widget_configure.*

/**
 * Created by arkadiusz on 23.03.18
 */

class WidgetConfigureActivity : AppCompatActivity() {

    private lateinit var viewModel: WidgetConfigureActivityViewModel

    private var appWidgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_widget_configure)
        setResult(RESULT_CANCELED)
        initViewModel()
        getAppWidgetIdFromBundle()
        setUpListAdapter()
        setUpTransparencySwitch()
    }

    private fun initViewModel() {
        viewModel = getViewModel(this)
    }

    private fun getAppWidgetIdFromBundle() {
        val extras = intent.extras
        extras?.let {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
    }

    private fun setUpListAdapter() {
        val eventsList = viewModel.getAllEvents()
        val adapter = WidgetConfigureAdapter(this, eventsList)
        eventListView.adapter = adapter
        eventListView.setOnItemClickListener { _, _, position, _ ->
            eventsList[position]?.id?.let {
                val chosenEvent = viewModel.getEventById(it)
                chosenEvent?.let {
                    viewModel.setWidgetIdForEvent(chosenEvent, appWidgetId)
                    setWidgetTransparencyIfSet(chosenEvent)
                    setUpWidgetUpdating()
                }
            }
        }
    }

    private fun setWidgetTransparencyIfSet(event: Event) {
        viewModel.setWidgetTransparencyFor(event, transparentSwitch.isChecked)
    }

    private fun setUpWidgetUpdating() {
        val appWidgetManager = AppWidgetManager.getInstance(baseContext)
        val views = RemoteViews(baseContext.packageName, R.layout.appwidget)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                applicationContext, AppWidgetProvider::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        sendBroadcast(intent)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    private fun setUpTransparencySwitch() {
        transparentSwitch.isChecked = false
    }
}