package com.arkadiusz.dayscounter.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.activities.DetailActivity;
import com.arkadiusz.dayscounter.database.Event;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;

/**
 * Created by Arkadiusz on 24.05.2017.
 */

public class AlarmBroadcast extends BroadcastReceiver {

  int id;
  String text;
  String date;
  String eventTitle;

  Realm realm;
  Event mEvent;


  public void onReceive(Context context, Intent intent) {
    id = intent.getIntExtra("eventId", 0);
    eventTitle = intent.getStringExtra("eventTitle");
    text = intent.getStringExtra("eventText");
    date = intent.getStringExtra("eventDate");


    Realm.init(context);
    RealmConfiguration config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);


    realm.executeTransaction(new Transaction() {
      @Override
      public void execute(Realm realm) {
          mEvent = realm.where(Event.class).equalTo("id",id).findFirst();
          mEvent.setHasAlarm(false);
      }
    });


    Intent intentSend = new Intent(context, DetailActivity.class);
    intentSend.putExtra("event_id", id);
    intentSend.putExtra("notificationClick", "clicked");

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addNextIntentWithParentStack(intentSend);

    PendingIntent pendingIntent = stackBuilder
        .getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

    int notificationColor = 0x34495e;


    String shortText ="";

    NotificationCompat.Builder mBuilder;

    if(text.length()>80) {
      for(int i=0;i<80;i++) {
        shortText+=text.charAt(i);
      }

      mBuilder =
          (Builder) new Builder(context)
              .setSmallIcon(R.drawable.n_icon)
              .setColor(notificationColor)
              .setContentTitle(eventTitle + " " + date)
              .setDefaults(Notification.DEFAULT_VIBRATE)
              .setContentIntent(pendingIntent)
              .setContentText(shortText);

      NotificationCompat.BigTextStyle style =
          new NotificationCompat.BigTextStyle(mBuilder);
      style.bigText(text);

    } else {
      mBuilder =
          (Builder) new Builder(context)
              .setSmallIcon(R.drawable.n_icon)
              .setColor(notificationColor)
              .setContentTitle(eventTitle + " " + date)
              .setDefaults(Notification.DEFAULT_VIBRATE)
              .setContentIntent(pendingIntent)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
              .setContentText(text);

      NotificationCompat.BigTextStyle style =
          new NotificationCompat.BigTextStyle(mBuilder);
      style.bigText(text);
    }

    NotificationManager mNotifyMgr =
        (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    mNotifyMgr.notify(id, mBuilder.build());
  }

}
