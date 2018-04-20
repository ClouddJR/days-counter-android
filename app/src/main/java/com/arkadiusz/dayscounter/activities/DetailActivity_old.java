package com.arkadiusz.dayscounter.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.database.Migration;
import com.arkadiusz.dayscounter.model.Event;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.io.File;

public class DetailActivity_old extends Activity {

  private int id;
  private ImageView mImageView;
  private TextView mTitleTextView;
  private TextView mDateTextView;
  private TextView mDescriptionTextView;
  private TextView mDateYearNumberTextView;
  private TextView mDateMonthNumberTextView;
  private TextView mRemiderDateTextView;
  private TextView mRepeatDateTextView;
  private TextView mDateDayNumberTextView;
  private ScrollView scrollView;

  private FloatingActionButton mEditEventFAB;
  private Realm realm;
  private RealmConfiguration config;
  private Event mEvent;
  private SharedPreferences mSharedPreferences;
  private boolean IsWithoutAds;
  File file;

  int yearsN = 0;
  int monthsN = 0;
  int daysN = 0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);

    //setUpRealm();
    //receiveID();
    setImage();
    //setInformations();
    getSharedPref();

  }

  public void setUpRealm() {
    Realm.init(this);
    config = new RealmConfiguration.Builder()
        .schemaVersion(3)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);
  }

  public void receiveID() {
    Intent intent = getIntent();
    id = intent.getIntExtra("event_id", 1);
    String isComingFromNotification = intent.getStringExtra("notificationClick");
    if (isComingFromNotification != null && isComingFromNotification.equals("clicked")) {
      NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      manager.cancel(id);
    }
  }

  public void setImage() {
    mImageView = (ImageView) findViewById(R.id.image);
    mEvent = realm.where(Event.class).equalTo("id", id).findFirst();
    if (mEvent == null) {
      Intent intent = new Intent(DetailActivity_old.this, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
      return;
    }
    if (mEvent.getImageID() == 0) {
      Uri uri = Uri.parse(mEvent.getImage());

      try {
        file = new File(uri.getPath());
      } catch (CursorIndexOutOfBoundsException e) {
        e.printStackTrace();
      }

      Glide.with(getApplicationContext()).load(file).into(mImageView);
    } else {
      Glide.with(this).load(mEvent.getImageID()).into(mImageView);
    }
  }

  public void setInformations() {

  }



  public void displayAd() {
    if (isNetworkAvailable()) {
      CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) scrollView
          .getLayoutParams();
      float scale = getResources().getDisplayMetrics().density;
      int dpAsPixels = (int) (50 * scale + 0.5f);
      layoutParams.setMargins(0, 0, 0, dpAsPixels);
      scrollView.setLayoutParams(layoutParams);
      final AdView mAdView = (AdView) findViewById(R.id.adView2);
      mAdView.setVisibility(View.VISIBLE);
      final AdRequest request = new AdRequest.Builder().build();

      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        public void run() {
          mAdView.loadAd(request);
        }
      }, 650);
    }
  }

  public String buildDate(int year, int month, int day, int hour, int minute) {
    String yearS = String.valueOf(year);
    String monthS = (month + 1 < 10) ? String.valueOf("0") + String.valueOf(month + 1)
        : String.valueOf(month + 1);
    String dayS = (day < 10) ? String.valueOf("0") + String.valueOf(day) : String.valueOf(day);
    String hourS = String.valueOf(hour);
    String minuteS =
        (minute < 10) ? String.valueOf("0") + String.valueOf(minute) : String.valueOf(minute);

    String date = yearS + "-" + monthS + "-" + dayS + " " + hourS + ":" + minuteS;

    return date;
  }


  public void getSharedPref() {
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    IsWithoutAds = mSharedPreferences.getBoolean("ads", false);
  }

  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager
        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

}
