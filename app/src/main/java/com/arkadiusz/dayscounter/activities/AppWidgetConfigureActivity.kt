package com.arkadiusz.dayscounter.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RemoteViews
import android.widget.TextView
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.WidgetConfigureAdapter
import com.arkadiusz.dayscounter.database.Event
import com.arkadiusz.dayscounter.providers.AppWidgetProvider
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_app_widget_configure.*

/**
 * Created by arkadiusz on 23.03.18
 */

class AppWidgetConfigureActivity : AppCompatActivity() {

    private val databaseRepository = DatabaseRepository()
    private lateinit var eventsList: RealmResults<Event>

    private var appWidgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_widget_configure)
        setResult(RESULT_CANCELED)
        getAllEvents()
        getAppWidgetIdFromBundle()
        setUpListAdapter()
        setUpTransparencySwitch()
    }

    private fun getAllEvents() {
        eventsList = databaseRepository.getAllEvents()
    }

    private fun getAppWidgetIdFromBundle() {
        val extras = intent.extras
        extras?.let {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
    }

    private fun setUpListAdapter() {
        val adapter = WidgetConfigureAdapter(this, eventsList)
        eventListView.adapter = adapter
        eventListView.setOnItemClickListener { _, view, _, _ ->
            val clickedView = view as TextView
            val chosenEventTitle = clickedView.text.toString()
            val chosenEvent = databaseRepository.getEventByName(chosenEventTitle)
            databaseRepository.setWidgetIdForEvent(chosenEvent, appWidgetId)
            setEventTransparentIfSet(chosenEvent)
            setUpWidgetUpdating()
        }
    }

    private fun setEventTransparentIfSet(event: Event) {
        when (transparentSwitch.isChecked) {
            true -> databaseRepository.setTransparentWidget(event)
            false -> databaseRepository.setInTransparentWidget(event)
        }
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