package com.arkadiusz.dayscounter.ui.addeditevent

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider
import com.arkadiusz.dayscounter.R
import kotlinx.android.synthetic.main.content_add.*

class EditActivity : BaseAddEditActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initWithPreviousEvent(intent.getStringExtra("eventId")!!)
        changeAddButtonName()
    }

    override fun handleSaveClick() {
        val event = viewModel.editEvent()
        addReminder(event)
        updateWidgetIfOnScreen(event.widgetID)
        finish()
    }

    private fun changeAddButtonName() {
        addButton.text = getString(R.string.add_activity_button_title)
    }

    private fun updateWidgetIfOnScreen(id: Int) {
        val intent = Intent(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            null,
            applicationContext,
            AppWidgetProvider::class.java
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
        sendBroadcast(intent)
    }
}