package com.arkadiusz.dayscounter.providers

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.util.TypedValue
import android.widget.RemoteViews
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.activities.DetailActivity
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.DateUtils
import com.arkadiusz.dayscounter.utils.FontUtils
import com.arkadiusz.dayscounter.utils.StorageUtils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.io.File


/**
 * Created by arkadiusz on 24.03.18
 */

class AppWidgetProvider : AppWidgetProvider() {

    private val databaseRepository = DatabaseRepository()

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        val widgetsCount = appWidgetIds?.size ?: 0

        for (i in 0 until widgetsCount) {
            val widgetId = appWidgetIds!![i]
            val event = databaseRepository.getEventByWidgetId(widgetId)
            event?.let {
                val counterText = getCounterText(event, context!!)
                val remoteViews = RemoteViews(context.packageName, R.layout.appwidget)

                displayTexts(remoteViews, context, event, counterText)
                setUpLineDivider(remoteViews, context, event)
                displayPicture(remoteViews, event, context, widgetId)
                setUpStackAndClickListener(event.id, context, remoteViews)

                appWidgetManager!!.updateAppWidget(widgetId, remoteViews)
            }
        }
    }

    private fun getCounterText(event: Event, context: Context): String {
        return DateUtils.calculateDate(event.date,
                event.formatYearsSelected,
                event.formatMonthsSelected,
                event.formatWeeksSelected,
                event.formatDaysSelected, context)
    }

    private fun displayTexts(remoteViews: RemoteViews, context: Context, event: Event, counterText: String) {
        remoteViews.setImageViewBitmap(R.id.eventCalculateText, getFontBitmap(context, counterText, event.counterFontSize.toFloat(), event.fontColor, event.fontType))
        remoteViews.setImageViewBitmap(R.id.eventTitle, getFontBitmap(context, event.name, event.titleFontSize.toFloat(), event.fontColor, event.fontType))
    }

    private fun setUpLineDivider(remoteViews: RemoteViews, context: Context, event: Event) {
        if (event.isLineDividerSelected) {
            val width = convertDipToPix(context, 120f)
            val height = convertDipToPix(context, 1.5f)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(event.fontColor)
            remoteViews.setImageViewBitmap(R.id.eventLine, bitmap)
        }
    }

    private fun displayPicture(remoteViews: RemoteViews, event: Event, context: Context, widgetId: Int) {
        if (!event.hasTransparentWidget) {
            when {
                event.imageColor != 0 -> displayPictureFromChosenColor(remoteViews, event, context, widgetId)
                event.imageID != 0 -> displayPictureFromBackgrounds(remoteViews, event, context, widgetId)
                else -> displayPictureFromGallery(remoteViews, event, context, widgetId)
            }
        }
    }

    private fun displayPictureFromChosenColor(remoteViews: RemoteViews, event: Event, context: Context, widgetId: Int) {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(event.imageColor)
        remoteViews.setImageViewBitmap(R.id.eventImage, bitmap)
    }

    private fun displayPictureFromBackgrounds(remoteViews: RemoteViews, event: Event, context: Context, widgetId: Int) {
        val pictureDim = event.pictureDim
        Picasso.with(context).load(event.imageID).transform(object : Transformation {
            override fun key(): String {
                return "darkening"
            }

            override fun transform(source: Bitmap?): Bitmap {
                val bitmap = source!!.copy(Bitmap.Config.ARGB_8888, true)
                val paint = Paint()
                val hexValue = 255 - (255 / 17 * pictureDim)
                val stringHex = Integer.toHexString(hexValue)
                paint.colorFilter = PorterDuffColorFilter("FF$stringHex$stringHex$stringHex".toLong(16).toInt(), PorterDuff.Mode.MULTIPLY)
                val canvas = Canvas(bitmap)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
                source.recycle()
                return bitmap
            }
        }).resize(0, 300).into(remoteViews, R.id.eventImage, intArrayOf(widgetId))
    }

    private fun displayPictureFromGallery(remoteViews: RemoteViews, event: Event, context: Context, widgetId: Int) {
        val file = File(StorageUtils.getRealPathFromURI(Uri.parse(event.image), context))
        val pictureDim = event.pictureDim
        Picasso.with(context).load(file).transform(object : Transformation {
            override fun key(): String {
                return "darkening"
            }

            override fun transform(source: Bitmap?): Bitmap {
                val bitmap = source!!.copy(Bitmap.Config.ARGB_8888, true)
                val paint = Paint()
                val hexValue = 255 - (255 / 17 * pictureDim)
                val stringHex = Integer.toHexString(hexValue)
                paint.colorFilter = PorterDuffColorFilter("FF$stringHex$stringHex$stringHex".toLong(16).toInt(), PorterDuff.Mode.MULTIPLY)
                val canvas = Canvas(bitmap)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
                source.recycle()
                return bitmap
            }
        }).resize(0, 250).into(remoteViews, R.id.eventImage, intArrayOf(widgetId))
    }


    private fun setUpStackAndClickListener(eventId: Int, context: Context?, remoteViews: RemoteViews) {
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra("event_id", eventId)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(intent)
        val pendingIntent = stackBuilder.getPendingIntent(eventId, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.eventImage, pendingIntent)
    }

    private fun getFontBitmap(context: Context, text: String, fontSizeSP: Float, color: Int, fontType: String): Bitmap {
        val fontSizePX = convertDipToPix(context, fontSizeSP)
        val pad = fontSizePX / 9
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.textSize = fontSizePX.toFloat()
        paint.typeface = FontUtils.getFontFor(fontType, context)

        val textWidth = (paint.measureText(text) + pad * 2).toInt()
        val height = (fontSizePX / 0.75).toInt()
        val bitmap = Bitmap.createBitmap(textWidth, height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        val xOriginal = pad.toFloat()
        canvas.drawText(text, xOriginal, fontSizePX.toFloat(), paint)
        return bitmap
    }

    private fun convertDipToPix(context: Context, dip: Float): Int {
        return TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                        context.resources.displayMetrics).toInt()
    }
}