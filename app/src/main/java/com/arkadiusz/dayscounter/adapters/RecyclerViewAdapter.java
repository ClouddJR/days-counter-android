package com.arkadiusz.dayscounter.adapters;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.model.Migration;
import com.arkadiusz.dayscounter.providers.AppWidgetProvider;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.utils.FontChangeCrawler;
import com.bumptech.glide.Glide;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;
import io.realm.RealmRecyclerViewAdapter;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class RecyclerViewAdapter extends RealmRecyclerViewAdapter<Event, ViewHolder> {


  private String LOG_TAG = RecyclerViewAdapter.class.getSimpleName();
  private Context context;
  private OrderedRealmCollection<Event> events;
  private FontChangeCrawler fontChanger;
  private Realm realm;
  int widgetID;
  private File file;


  public RecyclerViewAdapter(Context context, OrderedRealmCollection<Event> events) {
    super(context, events, true);
    this.context = context;
    this.events = events;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.single_event_layout, parent, false);
    ViewHolder vh = new ViewHolder(v);
    fontChanger = new FontChangeCrawler(context.getAssets(), "fonts/JosefinSans.ttf");
    fontChanger.replaceFonts((ViewGroup) v);
    return vh;
  }


  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final Event event = getData().get(position);
    if (event != null) {
      holder.mNameTextView.setText(event.getName());
      if (!event.getImage().equals("")) {
        Uri uri = Uri.parse(event.getImage());

        try {
          file = new File(getRealPathFromURI(uri));
        } catch (CursorIndexOutOfBoundsException e) {
          e.printStackTrace();
          holder.mImageView.setBackgroundColor(Color.BLACK);
        }

        Glide.with(context).load(file).into(holder.mImageView);
      }

      if (event.getImageID() != 0) {
        Glide.with(context).load(event.getImageID())
            .into(holder.mImageView);
      }

      String daysNumber = String.valueOf(calculateDays(event.getDate(), event.getType()));
      String properDays = String.valueOf(calculateProperDays(event.getDate(), event.getType()));
      switch (event.getType()) {
        case "past":
          if (daysNumber.equals("0")) {
            holder.mDaysNumberTextView
                .setText(context.getString(R.string.past_fragment_day_description_today));
          } else if (Integer.parseInt(daysNumber) > 0) {
            holder.mDaysNumberTextView.setText(
                properDays + " " + context.getString(R.string.past_fragment_day_description));
          } else if (Integer.parseInt(daysNumber) < 0) {
            moveToFuture(event.getId());
          }
          break;
        case "future":
          if (daysNumber.equals("0")) {
            holder.mDaysNumberTextView
                .setText(context.getString(R.string.future_fragment_day_description_today));
          } else if (Integer.parseInt(daysNumber) > 0) {
            holder.mDaysNumberTextView.setText(
                properDays + " " + context.getString(R.string.future_fragment_day_description));
          } else if (Integer.parseInt(daysNumber) < 0) {
            final Calendar c;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            Date date = null;
            try {
              date = format.parse(event.getDate());
            } catch (Exception e) {
              e.printStackTrace();
            }

            Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(2)
                .migration(new Migration())
                .build();
            realm = Realm.getInstance(config);

            c = toCalendar(date.getTime());
            switch (event.getRepeat()) {
              case "0":
                moveToPast(event.getId());
                break;
              case "1":
                realm.executeTransaction(new Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    event.setDate(repeatEvent(c, 1));
                  }
                });
                break;
              case "2":
                realm.executeTransaction(new Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    event.setDate(repeatEvent(c, 2));
                  }
                });
                break;
              case "3":
                realm.executeTransaction(new Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    event.setDate(repeatEvent(c, 3));
                  }
                });
                break;
              case "4":
                realm.executeTransaction(new Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    event.setDate(repeatEvent(c, 4));
                  }
                });
                break;
            }
          }
          break;
      }
      holder.mImageView
          .setColorFilter(Color.rgb(180, 180, 180), android.graphics.PorterDuff.Mode.MULTIPLY);
    }
  }


  private String repeatEvent(Calendar c, int type) {
    switch (type) {
      case 1:
        c.add(Calendar.DAY_OF_MONTH, 1);
        break;
      case 2:
        c.add(Calendar.DAY_OF_MONTH, 7);
        break;
      case 3:
        c.add(Calendar.MONTH, 1);
        break;
      case 4:
        c.add(Calendar.YEAR, 1);
        break;
    }

    String year = String.valueOf(c.get(Calendar.YEAR));
    String month = c.get(Calendar.MONTH)+1 < 10 ? "0" + String.valueOf(c.get(Calendar.MONTH)+1)
        : String.valueOf(c.get(Calendar.MONTH)+1);
    String day =
        c.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + String.valueOf(c.get(Calendar.DAY_OF_MONTH))
            : String.valueOf(c.get(Calendar.DAY_OF_MONTH));
    return year + "-" + month + "-" + day;
  }


  @Override
  public int getItemCount() {
    return events.size();
  }

  private int calculateDays(String dateS, String type) {
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

    // Get the represented date in milliseconds

    long milis1 = sDate.getTimeInMillis();
    long milis2 = eDate.getTimeInMillis();

    // Calculate difference in milliseconds
    long diff = milis2 - milis1;
    int days = (int) (diff / (24 * 60 * 60 * 1000));

    return days;
  }

  private int calculateProperDays(String dateS, String type) {
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


  public void moveToPast(final int id) {
    Realm.init(context);
    RealmConfiguration config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);

    Toast
        .makeText(context, context.getString(R.string.recycler_toast_moved_past),
            Toast.LENGTH_SHORT)
        .show();

    realm.executeTransaction(new Transaction() {
      @Override
      public void execute(Realm realm) {
        Event event = realm.where(Event.class).equalTo("id", id).findFirst();
        event.setType("past");
        widgetID = event.getWidgetID();
      }
    });

    if (widgetID != -1) {
      Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
          context, AppWidgetProvider.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID});
      context.sendBroadcast(intent);
    }
  }

  public void moveToFuture(final int id) {
    Realm.init(context);
    RealmConfiguration config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);

    Toast
        .makeText(context, context.getString(R.string.recycler_toast_moved_future),
            Toast.LENGTH_SHORT)
        .show();

    realm.executeTransaction(new Transaction() {
      @Override
      public void execute(Realm realm) {
        Event event = realm.where(Event.class).equalTo("id", id).findFirst();
        event.setType("future");
        widgetID = event.getWidgetID();
      }
    });

    if (widgetID != -1) {
      Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
          context, AppWidgetProvider.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID});
      context.sendBroadcast(intent);
    }

  }

  private String getRealPathFromURI(Uri contentURI) {
    String result;
    Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
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


}
