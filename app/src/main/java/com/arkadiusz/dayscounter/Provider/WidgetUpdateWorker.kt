package com.arkadiusz.dayscounter.Provider

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.arkadiusz.dayscounter.repositories.DatabaseProvider

class WidgetUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val eventsWithWidget = DatabaseProvider.provideRepository().getEventsWithWidgets()
        val widgetIds = eventsWithWidget.map { it.widgetID }.toIntArray()

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, appContext,
                AppWidgetProvider::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        appContext.sendBroadcast(intent)
        return Result.success(Data.Builder().putIntArray("widgetIds", widgetIds).build())
    }
}