package com.arkadiusz.dayscounter.providers;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.widget.RemoteViews;
import com.arkadiusz.dayscounter.activities.DetailActivity;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.model.Migration;
import com.arkadiusz.dayscounter.R;
import com.squareup.picasso.Picasso;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmError;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arkadiusz on 08.01.2017
 */

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

  private Realm realm;
  private RealmConfiguration config;
  RealmResults<Event> results;
  private String name;
  Event event;

  int yearsN = 0;
  int monthsN = 0;
  int daysN = 0;
  String years;
  String months;
  String days;
  String daysOnly;
  File file;


  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    final int N = appWidgetIds.length;
    try {
      Realm.init(context);
      config = new RealmConfiguration.Builder()
          .schemaVersion(2)
          .migration(new Migration())
          .build();
      realm = Realm.getInstance(config);
    } catch (RealmError e) {
      e.printStackTrace();
    }

    for (int i = 0; i < N; i++) {
      int appWidgetID = appWidgetIds[i];

      if (realm != null) {
        event = realm.where(Event.class).equalTo("widgetID", appWidgetID).findFirst();
      }

      if (event != null) {
        calculateDays(event.getDate(), event.getType());

        int id = event.getId();

        if (Locale.getDefault().getDisplayLanguage().equals("polski")) {
          years = "lat: ";
          months = "miesięcy: ";
          days = "dni: ";
          daysOnly = " dni";
        } else {
          years = "years: ";
          months = "months: ";
          days = "days: ";
          daysOnly = " days";
        }
        if (yearsN == 0) {
          yearsN++;
        }
        if (monthsN == 0) {
          monthsN++;
        }

        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("event_id", id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder
            .getPendingIntent(event.getId(), PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        views.setOnClickPendingIntent(R.id.image_widget, pendingIntent);

        views.setImageViewBitmap(R.id.title_widget,
            getFontBitmap(context, event.getName(), 16, event.getColor()));
        //views.setTextViewText(R.id.title_widget, event.getName());
        //views.setTextColor(R.id.title_widget, event.getColor());
        views.setInt(R.id.line, "setBackgroundColor", event.getColor());
        if (!true){
          //views.setTextViewText(R.id.days_widget,
          //years + String.valueOf(yearsN - 1) + "   " + months + String.valueOf(monthsN - 1)
          //+ "   " + days + String.valueOf(daysN));

          views.setImageViewBitmap(R.id.days_widget, getFontBitmap(context,
              years + String.valueOf(yearsN - 1) + "   " + months + String.valueOf(monthsN - 1)
                  + "   " + days + String.valueOf(daysN), 23, event.getColor()));
          //views.setTextColor(R.id.days_widget, event.getColor());
        } else{
          int days = calculateOnlyDays(event.getDate(), event.getType());
          //views.setTextViewText(R.id.days_widget, String.valueOf(days) + daysOnly);
          views.setImageViewBitmap(R.id.days_widget,
              getFontBitmap(context, String.valueOf(days) + daysOnly, 23, event.getColor()));
          //views.setTextColor(R.id.days_widget, event.getColor());
        }
        if (event.getImageID() != 0) {
          Picasso.with(context).load(event.getImageID()).resize(0, 300)
              .into(views, R.id.image_widget, new int[]{appWidgetID});
        } else {
          Uri uri = Uri.parse(event.getImage());

          try {
            file = new File(getRealPathFromURI(uri, context));
            Picasso.with(context)
                .load(file)
                .resize(0, 250)
                .into(views, R.id.image_widget, new int[]{appWidgetID});
          } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
          }

        }

        appWidgetManager.updateAppWidget(appWidgetID, views);
      }

    }
  }


  private void calculateDays(String dateS, String type) {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    yearsN = 0;
    monthsN = 0;
    daysN = 0;

    Calendar beginCalendar = Calendar.getInstance();
    Calendar finishCalendar = Calendar.getInstance();
    String year = String.valueOf(finishCalendar.get(Calendar.YEAR));
    String month = String.valueOf(finishCalendar.get(Calendar.MONTH) + 1);
    if (Integer.parseInt(month) < 10) {
      month = "0" + month;
    }
    String days = String.valueOf(finishCalendar.get(Calendar.DAY_OF_MONTH));
    if (Integer.parseInt(days) < 10) {
      days = "0" + days;
    }
    String today = year + "-" + month + "-" + days;

    if (type.equals("past")) {
      try {
        beginCalendar.setTime(format.parse(dateS));
        finishCalendar.setTime(format.parse(today));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      String tempDate = "";
      //calculating years
      while (beginCalendar.before(finishCalendar)) {
        tempDate = format.format(beginCalendar.getTime()).toUpperCase();
        yearsN++;
        beginCalendar.add(Calendar.YEAR, 1);
        if (beginCalendar.equals(finishCalendar)) {
          yearsN++;
          return;
        }
        beginCalendar.add(Calendar.YEAR, 1);
        if (beginCalendar.equals(finishCalendar)) {
          yearsN += 2;
          return;
        } else {
          beginCalendar.add(Calendar.YEAR, -1);
        }
      }

      try {
        beginCalendar.setTime(format.parse(tempDate));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      //calculating months
      while (beginCalendar.before(finishCalendar)) {
        tempDate = format.format(beginCalendar.getTime()).toUpperCase();
        monthsN++;
        beginCalendar.add(Calendar.MONTH, 1);
        if (beginCalendar.equals(finishCalendar)) {
          monthsN++;
          return;
        }
        beginCalendar.add(Calendar.MONTH, 1);
        if (beginCalendar.equals(finishCalendar)) {
          monthsN += 2;
          return;
        } else {
          beginCalendar.add(Calendar.MONTH, -1);
        }
      }

      try {
        beginCalendar.setTime(format.parse(tempDate));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      //calculating days
      while (beginCalendar.before(finishCalendar)) {
        tempDate = format.format(beginCalendar.getTime()).toUpperCase();
        daysN++;
        beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
      }

    } else {

      try {
        beginCalendar.setTime(format.parse(dateS));
        Calendar tempCalendar = beginCalendar;
        finishCalendar.setTime(format.parse(today));
        beginCalendar = finishCalendar;
        finishCalendar = tempCalendar;
      } catch (ParseException e) {
        e.printStackTrace();
      }

      String tempDate = "";
      //calculating years
      while (beginCalendar.before(finishCalendar)) {
        tempDate = format.format(beginCalendar.getTime()).toUpperCase();
        yearsN++;
        beginCalendar.add(Calendar.YEAR, 1);
        if (beginCalendar.equals(finishCalendar)) {
          yearsN++;
          return;
        }
        beginCalendar.add(Calendar.YEAR, 1);
        if (beginCalendar.equals(finishCalendar)) {
          yearsN += 2;
          return;
        } else {
          beginCalendar.add(Calendar.YEAR, -1);
        }
      }

      try {
        beginCalendar.setTime(format.parse(tempDate));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      //calculating months
      while (beginCalendar.before(finishCalendar)) {
        tempDate = format.format(beginCalendar.getTime()).toUpperCase();
        monthsN++;
        beginCalendar.add(Calendar.MONTH, 1);
        if (beginCalendar.equals(finishCalendar)) {
          monthsN++;
          return;
        }
        beginCalendar.add(Calendar.MONTH, 1);
        if (beginCalendar.equals(finishCalendar)) {
          monthsN += 2;
          return;
        } else {
          beginCalendar.add(Calendar.MONTH, -1);
        }
      }

      try {
        beginCalendar.setTime(format.parse(tempDate));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      //calculating days
      while (beginCalendar.before(finishCalendar)) {
        tempDate = format.format(beginCalendar.getTime()).toUpperCase();
        daysN++;
        beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
      }

    }


  }

  private int calculateOnlyDays(String dateS, String type) {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
    Date date = null;
    try {
      date = format.parse(dateS);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Calendar sDate;
    Calendar eDate;

    if (type.equals("past")) {
      sDate = toCalendar(date.getTime());
      eDate = toCalendar(System.currentTimeMillis());
    } else {
      sDate = toCalendar(System.currentTimeMillis());
      eDate = toCalendar(date.getTime());
    }

    int daysN = 0;
    while (sDate.before(eDate)) {
      sDate.add(Calendar.DAY_OF_MONTH, 1);
      daysN++;
    }

    return daysN;
  }

  private Calendar toCalendar(long timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar;
  }


  private String getRealPathFromURI(Uri contentURI, Context context) {
    String result;
    Cursor cursor = context.getContentResolver()
        .query(contentURI, null, null, null, null); //security exception
    if (cursor == null) { // Source is Dropbox or other similar local file path
      result = contentURI.getPath();
    } else {
      cursor.moveToFirst();
      int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
      result = cursor.getString(idx);
      cursor.close();
    }
    return result;
  }


  private Bitmap getFontBitmap(Context context, String text, float fontSizeSP, int color) {
    int fontSizePX = convertDiptoPix(context, fontSizeSP);
    int pad = (fontSizePX / 9);
    Paint paint = new Paint();
    Typeface typeface = Typeface
        .createFromAsset(context.getAssets(), "fonts/JosefinSans.ttf");
    paint.setAntiAlias(true);
    paint.setTypeface(typeface);
    paint.setColor(color);
    paint.setTextSize(fontSizePX);

    int textWidth = (int) (paint.measureText(text) + pad * 2);
    int height = (int) (fontSizePX / 0.75);
    Bitmap bitmap = Bitmap.createBitmap(textWidth, height, Bitmap.Config.ARGB_4444);
    Canvas canvas = new Canvas(bitmap);
    float xOriginal = pad;
    canvas.drawText(text, xOriginal, fontSizePX, paint);
    return bitmap;
  }

  private int convertDiptoPix(Context context, float dip) {
    int value = (int) TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
            context.getResources().getDisplayMetrics());
    return value;
  }


}
