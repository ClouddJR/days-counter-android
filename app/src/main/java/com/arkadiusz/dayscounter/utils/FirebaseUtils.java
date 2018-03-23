package com.arkadiusz.dayscounter.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.database.FirebaseTempObject;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

/**
 * Created by Arkadiusz on 21.07.2017.
 */

public class FirebaseUtils {

  public static void addToFirebase(DatabaseReference databaseReference, Event event,
      Context context, int id) {

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("id").setValue(id);

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("name").setValue(event.getName());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("date").setValue(event.getDate());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("image").setValue(event.getImage());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("type").setValue(event.getType());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("description").setValue(event.getDescription());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("imageID").setValue(event.getImageID());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("widgetID").setValue(event.getWidgetID());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("hasAlarm").setValue(String.valueOf(event.isHasAlarm()));

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("year").setValue(event.getReminderYear());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("month").setValue(event.getReminderMonth());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("day").setValue(event.getReminderDay());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("hour").setValue(event.getReminderHour());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("minute").setValue(event.getReminderMinute());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("notificationText").setValue(event.getNotificationText());

    databaseReference.child(SharedPreferencesUtils.getFirebaseEmail(context))
        .child("Event " + id + " " + event.getName() + " " + event.getDate())
        .child("repeat").setValue(event.getRepeat());
  }

  public static Event parseToEventObject(FirebaseTempObject tempObject) {
    Event event = new Event();
    event.setId(tempObject.getId());
    event.setName(tempObject.getName());
    event.setDate(tempObject.getDate());
    event.setImage(tempObject.getImage());
    event.setType(tempObject.getType());
    event.setDescription(tempObject.getDescription());
    event.setImageID(tempObject.getImageID());
    event.setWidgetID(tempObject.getWidgetID());

    event.setHasAlarm(tempObject.getHasAlarm().equals("true"));
    event.setReminderYear(tempObject.getYear());
    event.setReminderMonth(tempObject.getMonth());
    event.setReminderDay(tempObject.getDay());
    event.setReminderHour(tempObject.getHour());
    event.setReminderMinute(tempObject.getMinute());
    event.setNotificationText(tempObject.getNotificationText());
    event.setRepeat(tempObject.getRepeat());

    return event;
  }

  public static boolean isUnique(List<Event> firebaseEvents, Event event) {
    boolean isUnique = true;

    for (Event firebaseEvent : firebaseEvents) {
      if (event.getName().equals(firebaseEvent.getName()) &&
          event.getDate().equals(firebaseEvent.getDate()) &&
          event.getDescription().equals(firebaseEvent.getDescription())) {
        isUnique = false;
      }
    }
    return isUnique;
  }

  public static boolean isNetworkEnabled(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    return connectivityManager.getActiveNetworkInfo() != null && connectivityManager
        .getActiveNetworkInfo().isConnected();
  }

  public static void deletePreviousMail(String previousMail) {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    databaseReference.child(previousMail).removeValue();
  }

}
