package com.arkadiusz.dayscounter.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Arkadiusz on 20.07.2017.
 */

public class SharedPreferencesUtils {

  public static void setBlackTheme(Context context, String enableOrDisable) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("black-theme", enableOrDisable);
    editor.apply();
  }

  public static boolean isBlackTheme(Context context) {
    String isBlackTheme = PreferenceManager.getDefaultSharedPreferences(context)
        .getString("black-theme", "white");
    return isBlackTheme.equals("black");
  }

  public static void setFirebaseSynced(Context context, String syncedValue) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("firebase-synced", syncedValue);
    editor.apply();
  }

  public static boolean getFirebaseSynced(Context context) {
    String isSynced = PreferenceManager.getDefaultSharedPreferences(context)
        .getString("firebase-synced", "false");
    return isSynced.equals("true");
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

  public static void setDialogInfoSeen(Context context) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("dialog-seen","true");
    editor.apply();
  }

  public static boolean isDialogSeen(Context context) {
    String isSeen = PreferenceManager.getDefaultSharedPreferences(context).getString("dialog-seen","false");
    return isSeen.equals("true");
  }


}
