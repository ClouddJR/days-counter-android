package com.arkadiusz.dayscounter.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
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
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.model.Migration;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetailActivity extends Activity {

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

    setUpRealm();
    receiveID();
    setImage();
    if (mEvent == null) {
      return;
    }
    setInformations();
    getSharedPref();

//    if (!IsWithoutAds) {
//      displayAd();
//    }

  }

  @Override
  public void onEnterAnimationComplete() {
    super.onEnterAnimationComplete();
    int startScrollPos = 100;
    Animator animator = ObjectAnimator.ofInt(scrollView, "scrollY", startScrollPos)
        .setDuration(370);
    animator.setStartDelay(200);
    animator.start();
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
      Intent intent = new Intent(DetailActivity.this, MainActivity.class);
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
    scrollView = (ScrollView) findViewById(R.id.scrollView);
    mTitleTextView = (TextView) findViewById(R.id.eventTitle);
    mDateTextView = (TextView) findViewById(R.id.eventDate);
    mDescriptionTextView = (TextView) findViewById(R.id.eventDescription);
    mDateYearNumberTextView = (TextView) findViewById(R.id.date_years_number);
    mDateMonthNumberTextView = (TextView) findViewById(R.id.date_months_number);
    mDateDayNumberTextView = (TextView) findViewById(R.id.date_days_number);
    mRemiderDateTextView = (TextView) findViewById(R.id.reminder_date);
    mRepeatDateTextView = (TextView) findViewById(R.id.repeat_text);
    mEditEventFAB = (FloatingActionButton) findViewById(R.id.editEventButton);

    mTitleTextView.setText(mEvent.getName());
    mDateTextView.setText(mEvent.getDate());
    if (mEvent.getDescription().equals("")) {
      CardView cardView = (CardView) findViewById(R.id.card_description);
      cardView.setVisibility(View.GONE);
    } else {
      mDescriptionTextView.setText(mEvent.getDescription());
    }
    calculateDays(mEvent.getDate(), mEvent.getType());

    if (yearsN == 0) {
      yearsN++;
    }
    if (monthsN == 0) {
      monthsN++;
    }

    mDateYearNumberTextView.setText(String.valueOf(yearsN - 1));
    mDateMonthNumberTextView.setText(String.valueOf(monthsN - 1));
    mDateDayNumberTextView.setText(String.valueOf(daysN));

    if (mEvent.isHasAlarm()) {
      mRemiderDateTextView.setText(
          buildDate(mEvent.getReminderYear(), mEvent.getReminderMonth() - 1,
              mEvent.getReminderDay(), mEvent.getReminderHour(),
              mEvent.getReminderMinute()));
    } else {
      mRemiderDateTextView.setText(getString(R.string.detail_activity_reminder));
    }

    String repeat = "";
    switch (mEvent.getRepeat()) {
      case "0":
        repeat = getString(R.string.detail_once);
        break;
      case "1":
        repeat = getString(R.string.detail_daily);
        break;
      case "2":
        repeat = getString(R.string.detail_weekly);
        break;
      case "3":
        repeat = getString(R.string.detail_monthly);
        break;
      case "4":
        repeat = getString(R.string.detail_yearly);
        break;
    }

    mRepeatDateTextView.setText(repeat);

    mEditEventFAB.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(DetailActivity.this, AddActivity_old.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("id", id);
        startActivity(intent);
        finish();
      }
    });

  }

  private void calculateDays(String dateS, String type) {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

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
