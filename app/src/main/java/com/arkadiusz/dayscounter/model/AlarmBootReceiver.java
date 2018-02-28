package com.arkadiusz.dayscounter.model;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.arkadiusz.dayscounter.database.Event;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import java.util.Calendar;

/**
 * Created by Arkadiusz on 24.05.2017.
 */

public class AlarmBootReceiver extends BroadcastReceiver {

  Realm realm;
  RealmResults<Event> results;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
      Realm.init(context);
      RealmConfiguration config = new RealmConfiguration.Builder()
          .schemaVersion(2)
          .migration(new Migration())
          .build();
      realm = Realm.getInstance(config);
      results = realm.where(Event.class).equalTo("hasAlarm",true).findAll();

      for(Event event:results) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.clear();
        c.set(event.getYear(),event.getMonth()-1,event.getDay(),event.getHour(),event.getMinute());

        Intent intentSend = new Intent(context, AlarmBroadcast.class);
        intentSend.putExtra("eventTitle",event.getName());
        intentSend.putExtra("eventText",event.getNotificationText());
        intentSend.putExtra("eventId",event.getId());
        intentSend.putExtra("eventDate",event.getDate());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,event.getId(),intentSend,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);

      }
    }
  }
}
