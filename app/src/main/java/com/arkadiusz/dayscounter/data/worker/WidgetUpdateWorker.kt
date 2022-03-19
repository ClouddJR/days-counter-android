package com.arkadiusz.dayscounter.data.worker

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val databaseRepository: DatabaseRepository,
) : Worker(appContext, workerParams) {

    companion object {
        const val PERIODIC_WORK_WIDGET_UPDATE = "widget_update"
    }

    override fun doWork(): Result {
        val eventsWithWidget = databaseRepository.getEventsWithWidgets()
        val widgetIds = eventsWithWidget.map { it.widgetID }.toIntArray()

        val intent = Intent(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, appContext,
            AppWidgetProvider::class.java
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        appContext.sendBroadcast(intent)

        databaseRepository.closeDatabase()

        return Result.success(Data.Builder().putIntArray("widgetIds", widgetIds).build())
    }
}