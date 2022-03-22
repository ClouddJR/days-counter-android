package com.arkadiusz.dayscounter.Provider

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import android.widget.RemoteViews
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.ui.eventdetails.DetailActivity
import com.arkadiusz.dayscounter.util.DateUtils
import com.arkadiusz.dayscounter.util.DimTransformation
import com.arkadiusz.dayscounter.util.FontUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AppWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        appWidgetIds?.forEach { widgetId ->
            val event = databaseRepository.getEventByWidgetId(widgetId)
            event?.let {
                val counterText = getCounterText(event, context!!)

                val remoteViews = RemoteViews(
                    context.packageName,
                    if (event.lineDividerSelected) R.layout.appwidget else R.layout.appwidget_noline
                )

                displayTexts(remoteViews, context, event, counterText)
                setUpLineDivider(remoteViews, context, event)
                displayPicture(remoteViews, event, context, widgetId)
                setUpStackAndClickListener(event.id, context, remoteViews)

                appWidgetManager!!.updateAppWidget(widgetId, remoteViews)
            }
        }

        databaseRepository.closeDatabase()
    }

    private fun getCounterText(event: Event, context: Context): String {
        return DateUtils.calculateDate(
            event.date,
            event.formatYearsSelected,
            event.formatMonthsSelected,
            event.formatWeeksSelected,
            event.formatDaysSelected,
            context.resources
        )
    }

    private fun displayTexts(
        remoteViews: RemoteViews,
        context: Context,
        event: Event,
        counterText: String
    ) {
        remoteViews.setImageViewBitmap(
            R.id.eventCalculateText,
            getFontBitmap(
                context,
                counterText,
                event.counterFontSize.toFloat(),
                event.fontColor,
                event.fontType
            )
        )
        remoteViews.setImageViewBitmap(
            R.id.eventTitle,
            getFontBitmap(
                context,
                event.name,
                event.titleFontSize.toFloat(),
                event.fontColor,
                event.fontType
            )
        )
    }

    private fun getFontBitmap(
        context: Context,
        text: String,
        fontSizeSP: Float,
        color: Int,
        fontType: String
    ): Bitmap {
        val fontSizePX = convertDipToPix(context, fontSizeSP)
        val pad = fontSizePX / 9
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.textSize = fontSizePX.toFloat()
        paint.typeface = FontUtils.getFontFor(fontType, context)

        val textWidth = (paint.measureText(text) + pad * 2).toInt()
        val height = (fontSizePX / 0.75).toInt()
        val bitmap = Bitmap.createBitmap(textWidth, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val xOriginal = pad.toFloat()
        canvas.drawText(text, xOriginal, fontSizePX.toFloat(), paint)
        return bitmap
    }

    private fun setUpLineDivider(remoteViews: RemoteViews, context: Context, event: Event) {
        if (event.lineDividerSelected) {
            val width = convertDipToPix(context, 120f)
            val height = convertDipToPix(context, 1.5f)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(event.fontColor)
            remoteViews.setImageViewBitmap(R.id.eventLine, bitmap)
        }
    }

    private fun displayPicture(
        remoteViews: RemoteViews,
        event: Event,
        context: Context,
        widgetId: Int
    ) {
        when {
            event.hasTransparentWidget -> return
            event.imageColor != 0 -> displayPictureFromChosenColor(
                remoteViews,
                event,
            )
            event.imageID != 0 -> displayPictureFromBackgrounds(
                remoteViews,
                event,
                context,
                widgetId
            )
            else -> displayPictureFromGallery(remoteViews, event, context, widgetId)
        }
    }

    private fun displayPictureFromChosenColor(
        remoteViews: RemoteViews,
        event: Event,
    ) {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(event.imageColor)
        remoteViews.setImageViewBitmap(R.id.eventImage, bitmap)
    }

    private fun displayPictureFromBackgrounds(
        remoteViews: RemoteViews,
        event: Event,
        context: Context,
        widgetId: Int
    ) {
        Glide.with(context)
            .asBitmap()
            .load(event.imageID)
            .transform(DimTransformation(event.pictureDim))
            .into(AppWidgetTarget(context, R.id.eventImage, remoteViews, widgetId))
    }

    private fun displayPictureFromGallery(
        remoteViews: RemoteViews,
        event: Event,
        context: Context,
        widgetId: Int
    ) {
        var request = Glide.with(context)
            .asBitmap()
            .override(800, 600)
            .transform(DimTransformation(event.pictureDim))

        val file = File(event.image)
        request = when (file.exists()) {
            true -> request.load(file)
            false -> request.load(FirebaseStorage.getInstance().getReference(event.imageCloudPath))
        }

        request.into(AppWidgetTarget(context, R.id.eventImage, remoteViews, widgetId))
    }


    private fun setUpStackAndClickListener(
        eventId: String,
        context: Context?,
        remoteViews: RemoteViews
    ) {
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra("event_id", eventId)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(intent)

        val pendingIntent = stackBuilder.getPendingIntent(eventId.hashCode(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        remoteViews.setOnClickPendingIntent(R.id.eventImage, pendingIntent)
    }

    private fun convertDipToPix(context: Context, dip: Float): Int {
        return TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip,
                context.resources.displayMetrics
            ).toInt()
    }
}