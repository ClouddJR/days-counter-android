package com.arkadiusz.dayscounter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Arkadiusz on 20.07.2017.
 */

public class SharedPreferencesUtils {

  public static void setFirebaseSynced(Context context, String syncedValue) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("firebase-synced", syncedValue);
    editor.apply();
  }

  public static void setFirebaseEmail(Context context, String email) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("firebase-email", email);
    editor.apply();
  }

  public static String getFirebaseEmail(Context context) {
    String email = PreferenceManager.getDefaultSharedPreferences(context)
        .getString("firebase-email", "");
    return email;
  }



}
