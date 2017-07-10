package com.arkadiusz.dayscounter.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.arkadiusz.dayscounter.Database.Event;
import com.arkadiusz.dayscounter.Model.AlarmBroadcast;
import com.arkadiusz.dayscounter.Model.Migration;
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider;
import com.arkadiusz.dayscounter.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class AddActivity extends AppCompatActivity {

  private final int PICK_PHOTO_CUSTOM = 1;
  private final int PICK_PHOTO_GALLERY = 2;

  private EditText mNameEditText;
  private EditText mDateEditText;
  private EditText mDescritptionEditText;
  private EditText mReminderDateEditText;
  private EditText mReminderTextEditText;
  private TextView mClearTextView;
  private Button mAddButton;
  private ImageView mImageView;
  private String[] options;
  private Event mEvent;
  private Realm realm;
  private String eventType;
  private String mode;
  private int id;
  private Uri imageUri = null;
  private int imageID = 0;
  private SharedPreferences mSharedPreferences;
  private boolean IsWithoutAds;
  private boolean isDatePicked = false;
  private Spinner spinner;
  private int yearNotification;
  private int monthNotification;
  private int dayNotification;
  private int hourNotification;
  private int minuteNotification;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    setUpDialogOptions();
    setUpSpinner();
    setUpButtons();
    receiveEventType();
    setUpRealm();
    setUpIfEdit();
    getSharedPref();

    if (!IsWithoutAds) {
      displayAd();
    }

  }


  public void setUpButtons() {
    mClearTextView = (TextView) findViewById(R.id.clear);
    mNameEditText = (EditText) findViewById(R.id.name_editText);
    mDateEditText = (EditText) findViewById(R.id.date_editText);
    mDescritptionEditText = (EditText) findViewById(R.id.description_editText);
    mReminderDateEditText = (EditText) findViewById(R.id.reminder_date);
    mReminderTextEditText = (EditText) findViewById(R.id.reminder_text);

    mClearTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mReminderTextEditText.setText("");
        mReminderDateEditText.setText("");
      }
    });

    View.OnClickListener showDatePicker = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final View vv = v;

        DatePickerDialog dialog = new DatePickerDialog(AddActivity.this, new OnDateSetListener() {
          boolean mFirst = true;

          @Override
          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String yearText = String.valueOf(year);
            String monthOfYearText = String.valueOf(monthOfYear + 1);
            if (Integer.valueOf(monthOfYearText) < 10) {
              monthOfYearText = "0" + monthOfYearText;
            }
            String dayOfMonthText = String.valueOf(dayOfMonth);
            if (dayOfMonth < 10) {
              dayOfMonthText = "0" + dayOfMonthText;
            }
            String date = yearText + "-" + monthOfYearText + "-" + dayOfMonthText;
            if (mFirst) {
              mFirst = false;
              if (vv.getId() == R.id.date_editText) {
                mDateEditText.setText(date);
              } else {
                yearNotification = year;
                monthNotification = monthOfYear + 1;
                dayNotification = dayOfMonth;
                mReminderDateEditText.setText(date);
                TimePickerDialog timeDialog = new TimePickerDialog(AddActivity.this,
                    new OnTimeSetListener() {
                      boolean mFirst = true;

                      @Override
                      public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (mFirst) {
                          hourNotification = hourOfDay;
                          minuteNotification = minute;
                          mFirst = false;
                          String date = mReminderDateEditText.getText().toString();
                          mReminderDateEditText.setText("");
                          String hourText = String.valueOf(hourOfDay);
                          String minuteText = String.valueOf(minute);
                          if (minute < 10) {
                            minuteText = "0" + minuteText;
                          }
                          String dateAndHour = date + " " + hourText + ":" + minuteText;
                          mReminderDateEditText.setText(dateAndHour);
                        } else {
                        }
                      }
                    }, hour, minute, DateFormat.is24HourFormat(AddActivity.this));
                timeDialog.show();
              }
            }
          }
        }, year, month, day);
        dialog.show();


      }
    };

    mDateEditText.setOnClickListener(showDatePicker);

    mReminderDateEditText.setOnClickListener(showDatePicker);

    mAddButton = (Button) findViewById(R.id.add_button);
    mAddButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final Event event = new Event();

        if (mode.equals("edit")) {
          if (validateFormInEditMode()) {
            event.setId(id);
            event.setName(mNameEditText.getText().toString().trim());
            event.setType(mEvent.getType());
            event.setDate(mDateEditText.getText().toString());
            if (imageUri != null) {
              event.setImage(imageUri.toString());
              event.setImageID(0);
            } else if (imageID != 0) {
              event.setImageID(imageID);
              event.setImage("");
            } else {
              if (!mEvent.getImage().equals("")) {
                event.setImage(mEvent.getImage());
                event.setImageID(0);
              } else if (mEvent.getImageID() != 0) {
                event.setImage("");
                event.setImageID(mEvent.getImageID());
              }
            }

            event.setType(mEvent.getType());

            if (!mDescritptionEditText.getText().toString().equals("")) {
              event.setDescription(mDescritptionEditText.getText().toString());
            } else {
              event.setDescription("");
            }

            int widgetID = mEvent.getWidgetID();
            int color = mEvent.getColor();
            boolean isOnlyDays = mEvent.isOnlyDays();
            if (widgetID != 0) {
              event.setWidgetID(widgetID);
            }

            if (color != 0) {
              event.setColor(color);
            }

            if (isOnlyDays) {
              event.setOnlyDays(true);
            }

            Intent intent = new Intent(AddActivity.this, AlarmBroadcast.class);
            if (!mReminderDateEditText.getText().toString().equals("")) {
              event.setHasAlarm(true);
              event.setYear(yearNotification);
              event.setMonth(monthNotification);
              event.setDay(dayNotification);
              event.setHour(hourNotification);
              event.setMinute(minuteNotification);
              event.setNotificationText(mReminderTextEditText.getText().toString());

              Calendar c = Calendar.getInstance();
              c.setTimeInMillis(System.currentTimeMillis());
              c.clear();
              c.set(yearNotification, monthNotification - 1, dayNotification, hourNotification,
                  minuteNotification);

              intent = new Intent(AddActivity.this, AlarmBroadcast.class);
              intent.putExtra("eventTitle", event.getName());
              intent.putExtra("eventText", event.getNotificationText());
              intent.putExtra("eventId", event.getId());
              intent.putExtra("eventDate", event.getDate());

              PendingIntent pendingIntent = PendingIntent
                  .getBroadcast(AddActivity.this, event.getId(), intent,
                      PendingIntent.FLAG_UPDATE_CURRENT);
              AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
              alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            } else {
              event.setHasAlarm(false);
              PendingIntent.getBroadcast(AddActivity.this, event.getId(), intent,
                  PendingIntent.FLAG_UPDATE_CURRENT).cancel();
            }

            int spinnerItemID = spinner.getSelectedItemPosition();
            event.setRepeat(String.valueOf(spinnerItemID));

            realm.executeTransaction(new Transaction() {
              @Override
              public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(event);
              }
            });

            intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                getApplicationContext(), AppWidgetProvider.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID});
            sendBroadcast(intent);

            finish();

          }
        } else {

          int nextID = 1;
          try {
            nextID = realm.where(Event.class).max("id").intValue();
            nextID += 1;
          } catch (Exception e) {
            e.printStackTrace();
          }

          if (validateForm()) {
            event.setId(nextID);
            event.setName(mNameEditText.getText().toString().trim());
            event.setType(eventType);
            event.setDate(mDateEditText.getText().toString());
            if (imageUri != null) {
              event.setImage(imageUri.toString());
              event.setImageID(0);
            } else {
              event.setImageID(imageID);
              event.setImage("");
            }

            if (!mDescritptionEditText.getText().toString().equals("")) {
              event.setDescription(mDescritptionEditText.getText().toString());
            } else {
              event.setDescription("");
            }

            if (!mReminderDateEditText.getText().toString().equals("")) {
              event.setHasAlarm(true);
              event.setYear(yearNotification);
              event.setMonth(monthNotification);
              event.setDay(dayNotification);
              event.setHour(hourNotification);
              event.setMinute(minuteNotification);
              event.setNotificationText(mReminderTextEditText.getText().toString());

              Calendar c = Calendar.getInstance();
              c.setTimeInMillis(System.currentTimeMillis());
              c.clear();
              c.set(yearNotification, monthNotification - 1, dayNotification, hourNotification,
                  minuteNotification);

              Intent intent = new Intent(AddActivity.this, AlarmBroadcast.class);
              intent.putExtra("eventTitle", event.getName());
              intent.putExtra("eventText", event.getNotificationText());
              intent.putExtra("eventId", event.getId());
              intent.putExtra("eventDate", event.getDate());

              PendingIntent pendingIntent = PendingIntent
                  .getBroadcast(AddActivity.this, event.getId(), intent,
                      PendingIntent.FLAG_UPDATE_CURRENT);
              AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
              alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            }

            int spinnerItemID = spinner.getSelectedItemPosition();
            event.setRepeat(String.valueOf(spinnerItemID));

            realm.executeTransaction(new Transaction() {
              @Override
              public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(event);
              }
            });

            finish();

          }
        }

      }
    });

    mImageView = (ImageView) findViewById(R.id.event_image);
    ImageView clickImage = (ImageView) findViewById(R.id.choose_image);
    clickImage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          if (ActivityCompat
              .checkSelfPermission(AddActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_PHOTO_CUSTOM);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle(getString(R.string.add_activity_dialog_title));
        builder.setItems(options, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
              case 0:
                CropImage.startPickImageActivity(AddActivity.this);
                break;
              case 1:
                Intent intent = new Intent(AddActivity.this, GalleryActivity.class);
                startActivityForResult(intent, PICK_PHOTO_GALLERY);
                break;
            }
          }
        });
        builder.show();
      }
    });
  }

  private void setUpSpinner() {
    spinner = (Spinner) findViewById(R.id.spinner_repeat);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.add_activity_repeat, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  @Override
  @SuppressLint("NewApi")
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
        && resultCode == Activity.RESULT_OK) {
      Uri tempImageUri = CropImage.getPickImageResultUri(this, data);
      imageUri = tempImageUri;

      // For API >= 23 we need to check specifically that we have permissions to read external storage.
      if (VERSION.SDK_INT >= 23) {
        if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
          // request permissions and handle the result in onRequestPermissionsResult()
          imageUri = tempImageUri;
          requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
              CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
        } else {
          // no permissions required or already grunted, can start crop image activity
          startCropImageActivity(imageUri);
        }
      } else {
        startCropImageActivity(imageUri);
      }

      File file = null;
      if (!imageUri.toString().equals("")) {
        file = new File(tempImageUri.getPath());
      }

      if (file != null) {
        Picasso.with(this)
            .load(file)
            .fit()
            .centerCrop()
            .into(mImageView);
      }

    }

    if (requestCode == PICK_PHOTO_CUSTOM && resultCode == Activity.RESULT_OK) {
      if (data == null) {
        return;
      } else {
        imageUri = data.getData();
        imageID = 0;

        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);

      }
    }

    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);
      if (resultCode == RESULT_OK) {
        imageUri = result.getUri();
        savefile(imageUri);

        File file = new File(imageUri.getPath());

        Picasso.with(this)
            .load(file)
            .fit()
            .centerCrop()
            .into(mImageView);

      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Exception error = result.getError();
        error.printStackTrace();
      }
    }

  }

  public void onRequestPermissionsResult(int requestCode, String permissions[],
      int[] grantResults) {
    if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
      if (imageUri != null && grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // required permissions granted, start crop image activity
        startCropImageActivity(imageUri);
      } else {
        Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG)
            .show();
      }
    }
  }


  public void setUpRealm() {
    Realm.init(this);
    RealmConfiguration config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);
  }

  public void receiveEventType() {
    Intent intent = getIntent();
    mode = intent.getStringExtra("mode");
    if (mode == null) {
      mode = "normal";
    }
    id = intent.getIntExtra("id", 1);
    eventType = intent.getStringExtra("Event Type");
  }

  public boolean validateForm() {
    if (mNameEditText.getText().toString().equals("")) {
      Toast.makeText(this, getString(R.string.add_activity_toast_name), Toast.LENGTH_SHORT).show();
      return false;
    }

    if (mDateEditText.getText().toString().equals("")) {
      Toast.makeText(this, getString(R.string.add_activity_toast_date), Toast.LENGTH_SHORT).show();
      return false;
    }

    if (mNameEditText.getText().length() > 30) {
      Toast.makeText(this, getString(R.string.add_activity_toast_name_length), Toast.LENGTH_SHORT)
          .show();
      return false;
    }

    if (imageUri == null && imageID == 0) {
      Toast.makeText(this, getString(R.string.add_activity_toast_image), Toast.LENGTH_SHORT).show();
      return false;
    }

    if (imageUri != null) {
      if (imageUri.toString().contains("com.google.android.apps.photos")) {
        Toast.makeText(this, getString(R.string.add_activity_google_photos), Toast.LENGTH_SHORT)
            .show();
        return false;
      }
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
    try {
      format.parse(mDateEditText.getText().toString());
    } catch (ParseException e) {
      Toast.makeText(this, getString(R.string.add_activity_date), Toast.LENGTH_SHORT).show();
      return false;
    }

    return true;
  }

  public boolean validateFormInEditMode() {
    if (mNameEditText.getText().toString().equals("")) {
      Toast.makeText(this, getString(R.string.add_activity_toast_name), Toast.LENGTH_SHORT).show();
      return false;
    }

    if (mNameEditText.getText().length() > 35) {
      Toast.makeText(this, getString(R.string.add_activity_toast_name_length), Toast.LENGTH_SHORT)
          .show();
      return false;
    }

    if (mDateEditText.getText().toString().equals("")) {
      Toast.makeText(this, getString(R.string.add_activity_toast_date), Toast.LENGTH_SHORT).show();
      return false;
    }

    if (imageUri == null && imageID == 0 && mEvent.getImage().equals("")
        && mEvent.getImageID() == 0) {
      Toast.makeText(this, getString(R.string.add_activity_toast_image), Toast.LENGTH_SHORT).show();
      return false;
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
    try {
      format.parse(mDateEditText.getText().toString());
    } catch (ParseException e) {
      Toast.makeText(this, getString(R.string.add_activity_date), Toast.LENGTH_SHORT).show();
      return false;
    }

    return true;
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    imageID = intent.getIntExtra("imageID", 0);
    if (imageID != 0) {
      imageUri = null;
      Picasso.with(getApplicationContext()).load(imageID).resize(0, 700).into(mImageView);
    }
  }

  public void setUpIfEdit() {
    if (mode.equals("edit")) {
      mEvent = realm.where(Event.class).equalTo("id", id).findFirst();
      mNameEditText.setText(mEvent.getName());
      mDateEditText.setText(mEvent.getDate());
      mDescritptionEditText.setText(mEvent.getDescription());
      if (mEvent.getImageID() != 0) {
        Picasso.with(this).load(mEvent.getImageID()).resize(0, 700).into(mImageView);
      } else {
        Uri uri = Uri.parse(mEvent.getImage());
        File file = new File(uri.getPath());
        Picasso.with(this)
            .load(file)
            .resize(0, 700)
            .into(mImageView);
      }
      if (mEvent.hasAlarm()) {
        String date =
            mEvent.getYear() + "-" + mEvent.getMonth() + "-" + mEvent.getDay() + " " + mEvent
                .getHour() + ":" + ((mEvent.getMinute() < 10) ? "0" + mEvent.getMinute()
                : mEvent.getMinute());
        mReminderDateEditText.setText(date);
        mReminderTextEditText.setText(mEvent.getNotificationText());
      }
      spinner.setSelection(Integer.parseInt(mEvent.getRepeat()));
      mAddButton.setText(R.string.add_activity_button_title);
      getSupportActionBar().setTitle(R.string.add_activity_toolbar_title);
    }
  }

  public void displayAd() {
    if (isNetworkAvailable()) {
      final AdView mAdView = (AdView) findViewById(R.id.adView);
      mAdView.setVisibility(View.VISIBLE);
      final AdRequest request = new AdRequest.Builder().build();

      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          mAdView.loadAd(request);
        }
      }, 500);
    }
  }

  public void setUpDialogOptions() {
    options = new String[2];
    options[0] = getString(R.string.add_activity_dialog_option_custom);
    options[1] = getString(R.string.add_activity_dialog_option_gallery);
  }

  private void startCropImageActivity(Uri imageUri) {
    CropImage.activity(imageUri)
        .start(this);
  }

  void savefile(Uri sourceuri) {

    File folder = new File(Environment.getExternalStorageDirectory() + "/croppedImages");
    boolean success = true;
    if (!folder.exists()) {
      success = folder.mkdir();
    }

    String sourceFilename = sourceuri.getPath();
    String destinationFilename =
        android.os.Environment.getExternalStorageDirectory().getPath() + File.separatorChar
            + "croppedImages/" + sourceuri.getLastPathSegment();

    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;

    try {
      bis = new BufferedInputStream(new FileInputStream(sourceFilename));
      bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
      byte[] buf = new byte[1024];
      bis.read(buf);
      do {
        bos.write(buf);
      } while (bis.read(buf) != -1);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bis != null) {
          bis.close();
        }
        if (bos != null) {
          bos.close();
        }
        imageUri = Uri.parse(destinationFilename);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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
