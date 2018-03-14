package com.arkadiusz.dayscounter.activities;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.database.Event;
import com.google.firebase.database.DatabaseReference;
import io.realm.Realm;


public class AddActivity_old extends AppCompatActivity {

  private final int PICK_PHOTO_CUSTOM = 1;
  private final int PICK_PHOTO_GALLERY = 2;

  private DatabaseReference mDatabaseReference;
  private EditText mNameEditText;
  private EditText mDateEditText;
  private EditText mDescritptionEditText;
  private EditText mReminderDateEditText;
  private EditText mReminderTextEditText;
  private TextView mClearTextView;
  private Button mAddButton;
  private Spinner spinner;
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
  private int yearNotification = 0;
  private int monthNotification = 0;
  private int dayNotification = 0;
  private int hourNotification = 0;
  private int minuteNotification = 0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.content_add_r);
//    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//    setSupportActionBar(toolbar);
//    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//    setUpDialogOptions();
//    setUpSpinner();
//    setUpButtons();
//    receiveEventType();
//    setUpRealm();
//    setUpIfEdit();
//    getSharedPref();
//    setUpFireBase();
//
//    if (!IsWithoutAds) {
//      displayAd();
//    }

  }

//  @Override
//  public void onBackPressed() {
//    AlertDialog.Builder builder;
//    if (anyFieldIsNotEmpty()) {
//      builder = new AlertDialog.Builder(this);
//      builder.setTitle(getString(R.string.add_activity_back_button_title));
//      builder.setMessage(getString(R.string.add_activity_back_button_message));
//      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialogInterface, int i) {
//          AddActivity_old.super.onBackPressed();
//        }
//      });
//      builder.setNegativeButton(getString(R.string.add_activity_back_button_cancel),
//          new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//              return;
//            }
//          });
//      builder.show();
//    } else {
//      AddActivity_old.super.onBackPressed();
//    }
//  }
//
//
//  public void setUpButtons() {
//    mClearTextView = (TextView) findViewById(R.id.clear);
//    mNameEditText = (EditText) findViewById(R.id.name_editText);
//    mDateEditText = (EditText) findViewById(R.id.date_editText);
//    mDescritptionEditText = (EditText) findViewById(R.id.description_editText);
//    mReminderDateEditText = (EditText) findViewById(R.id.reminder_date);
//    mReminderTextEditText = (EditText) findViewById(R.id.reminder_text);
//
//    mClearTextView.setOnClickListener(new OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        mReminderTextEditText.setText("");
//        mReminderDateEditText.setText("");
//        yearNotification = 0;
//        monthNotification = 0;
//        dayNotification = 0;
//        hourNotification = 0;
//        minuteNotification = 0;
//      }
//    });
//
//    View.OnClickListener showDatePicker = new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        Calendar c = Calendar.getInstance();
//        final int year;
//        final int month;
//        final int day;
//        final int hour;
//        final int minute;
//        if (yearNotification == 0 && monthNotification == 0 && dayNotification == 0
//            && hourNotification == 0 && minuteNotification == 0) {
//          year = c.get(Calendar.YEAR);
//          month = c.get(Calendar.MONTH);
//          day = c.get(Calendar.DAY_OF_MONTH);
//          hour = c.get(Calendar.HOUR_OF_DAY);
//          minute = c.get(Calendar.MINUTE);
//        } else {
//          year = yearNotification;
//          month = monthNotification - 1;
//          day = dayNotification;
//          hour = hourNotification;
//          minute = minuteNotification;
//        }
//        final View vv = v;
//
//        DatePickerDialog dialog = new DatePickerDialog(AddActivity_old.this, new OnDateSetListener() {
//          boolean mFirst = true;
//
//          @Override
//          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            String yearText = String.valueOf(year);
//            String monthOfYearText = String.valueOf(monthOfYear + 1);
//            if (Integer.valueOf(monthOfYearText) < 10) {
//              monthOfYearText = "0" + monthOfYearText;
//            }
//            String dayOfMonthText = String.valueOf(dayOfMonth);
//            if (dayOfMonth < 10) {
//              dayOfMonthText = "0" + dayOfMonthText;
//            }
//            String date = yearText + "-" + monthOfYearText + "-" + dayOfMonthText;
//            if (mFirst) {
//              mFirst = false;
//              if (vv.getId() == R.id.date_editText) {
//                mDateEditText.setText(date);
//              } else {
//                yearNotification = year;
//                monthNotification = monthOfYear + 1;
//                dayNotification = dayOfMonth;
//                mReminderDateEditText.setText(date);
//                TimePickerDialog timeDialog = new TimePickerDialog(AddActivity_old.this,
//                    new OnTimeSetListener() {
//                      boolean mFirst = true;
//
//                      @Override
//                      public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        if (mFirst) {
//                          hourNotification = hourOfDay;
//                          minuteNotification = minute;
//                          mFirst = false;
//                          String date = mReminderDateEditText.getText().toString();
//                          mReminderDateEditText.setText("");
//                          String hourText = String.valueOf(hourOfDay);
//                          String minuteText = String.valueOf(minute);
//                          if (minute < 10) {
//                            minuteText = "0" + minuteText;
//                          }
//                          String dateAndHour = date + " " + hourText + ":" + minuteText;
//                          mReminderDateEditText.setText(dateAndHour);
//                        } else {
//                        }
//                      }
//                    }, hour, minute, DateFormat.is24HourFormat(AddActivity_old.this));
//                timeDialog.show();
//              }
//            }
//          }
//        }, year, month, day);
//        dialog.show();
//
//
//      }
//    };
//
//    mDateEditText.setOnClickListener(showDatePicker);
//
//    mReminderDateEditText.setOnClickListener(showDatePicker);
//
//    mAddButton = (Button) findViewById(R.id.add_button);
//    mAddButton.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        final Event event = new Event();
//
//        if (mode.equals("edit")) {
//          if (validateFormInEditMode()) {
//
//            Event previousEvent = realm.where(Event.class).equalTo("id", id).findFirst();
//            String previousName = previousEvent.getName();
//            String previousDate = previousEvent.getDate();
//
//            event.setId(id);
//            event.setName(mNameEditText.getText().toString().trim());
//            event.setType(mEvent.getType());
//            event.setDate(mDateEditText.getText().toString());
//            if (imageUri != null) {
//              event.setImage(imageUri.toString());
//              event.setImageID(0);
//            } else if (imageID != 0) {
//              event.setImageID(imageID);
//              event.setImage("");
//            } else {
//              if (!mEvent.getImage().equals("")) {
//                event.setImage(mEvent.getImage());
//                event.setImageID(0);
//              } else if (mEvent.getImageID() != 0) {
//                event.setImage("");
//                event.setImageID(mEvent.getImageID());
//              }
//            }
//
//            event.setType(mEvent.getType());
//
//            if (!mDescritptionEditText.getText().toString().equals("")) {
//              event.setDescription(mDescritptionEditText.getText().toString());
//            } else {
//              event.setDescription("");
//            }
//
//            int widgetID = mEvent.getWidgetID();
//            int color = mEvent.getColor();
//            boolean isOnlyDays = mEvent.isOnlyDays();
//            if (widgetID != 0) {
//              event.setWidgetID(widgetID);
//            }
//
//            if (color != 0) {
//              event.setColor(color);
//            }
//
//            if (isOnlyDays) {
//              event.setOnlyDays(true);
//            }
//
//            Intent intent = new Intent(AddActivity_old.this, AlarmBroadcast.class);
//            if (!mReminderDateEditText.getText().toString().equals("")) {
//              event.setHasAlarm(true);
//              event.setYear(yearNotification);
//              event.setMonth(monthNotification);
//              event.setDay(dayNotification);
//              event.setHour(hourNotification);
//              event.setMinute(minuteNotification);
//              event.setNotificationText(mReminderTextEditText.getText().toString());
//
//              Calendar c = Calendar.getInstance();
//              c.setTimeInMillis(System.currentTimeMillis());
//              c.clear();
//              c.set(yearNotification, monthNotification - 1, dayNotification, hourNotification,
//                  minuteNotification);
//
//              intent = new Intent(AddActivity_old.this, AlarmBroadcast.class);
//              intent.putExtra("eventTitle", event.getName());
//              intent.putExtra("eventText", event.getNotificationText());
//              intent.putExtra("eventId", event.getId());
//              intent.putExtra("eventDate", event.getDate());
//
//              PendingIntent pendingIntent = PendingIntent
//                  .getBroadcast(AddActivity_old.this, event.getId(), intent,
//                      PendingIntent.FLAG_UPDATE_CURRENT);
//              AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//              alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
//            } else {
//              event.setHasAlarm(false);
//              PendingIntent.getBroadcast(AddActivity_old.this, event.getId(), intent,
//                  PendingIntent.FLAG_UPDATE_CURRENT).cancel();
//            }
//
//            int spinnerItemID = spinner.getSelectedItemPosition();
//            event.setRepeat(String.valueOf(spinnerItemID));
//
//            realm.executeTransaction(new Transaction() {
//              @Override
//              public void execute(Realm realm) {
//                realm.copyToRealmOrUpdate(event);
//              }
//            });
//
//            if (!SharedPreferencesUtils.getFirebaseEmail(AddActivity_old.this).equals("")) {
//              String userMail = SharedPreferencesUtils.getFirebaseEmail(AddActivity_old.this);
//              mDatabaseReference.child(userMail).child("Event " + id + " " + previousName + " "
//                  + previousDate).removeValue();
//
//              if (!SharedPreferencesUtils.getFirebaseEmail(AddActivity_old.this).equals("")) {
//                FirebaseUtils
//                    .addToFirebase(mDatabaseReference, event, AddActivity_old.this, event.getId());
//              }
//            }
//
//            intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
//                getApplicationContext(), AppWidgetProvider.class);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID});
//            sendBroadcast(intent);
//
//            finish();
//
//          }
//        } else {
//
//          int nextID = 1;
//          try {
//            nextID = realm.where(Event.class).max("id").intValue();
//            nextID += 1;
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
//
//          if (validateForm()) {
//            event.setId(nextID);
//            event.setName(mNameEditText.getText().toString().trim());
//            event.setType(eventType);
//            event.setDate(mDateEditText.getText().toString());
//            if (imageUri != null) {
//              event.setImage(imageUri.toString());
//              event.setImageID(0);
//            } else {
//              event.setImageID(imageID);
//              event.setImage("");
//            }
//
//            if (!mDescritptionEditText.getText().toString().equals("")) {
//              event.setDescription(mDescritptionEditText.getText().toString());
//            } else {
//              event.setDescription("");
//            }
//
//            if (!mReminderDateEditText.getText().toString().equals("")) {
//              event.setHasAlarm(true);
//              event.setYear(yearNotification);
//              event.setMonth(monthNotification);
//              event.setDay(dayNotification);
//              event.setHour(hourNotification);
//              event.setMinute(minuteNotification);
//              event.setNotificationText(mReminderTextEditText.getText().toString());
//
//              Calendar c = Calendar.getInstance();
//              c.setTimeInMillis(System.currentTimeMillis());
//              c.clear();
//              c.set(yearNotification, monthNotification - 1, dayNotification, hourNotification,
//                  minuteNotification);
//
//              Intent intent = new Intent(AddActivity_old.this, AlarmBroadcast.class);
//              intent.putExtra("eventTitle", event.getName());
//              intent.putExtra("eventText", event.getNotificationText());
//              intent.putExtra("eventId", event.getId());
//              intent.putExtra("eventDate", event.getDate());
//
//              PendingIntent pendingIntent = PendingIntent
//                  .getBroadcast(AddActivity_old.this, event.getId(), intent,
//                      PendingIntent.FLAG_UPDATE_CURRENT);
//              AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//              alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
//            }
//
//            int spinnerItemID = spinner.getSelectedItemPosition();
//            event.setRepeat(String.valueOf(spinnerItemID));
//
//            realm.executeTransaction(new Transaction() {
//              @Override
//              public void execute(Realm realm) {
//                realm.copyToRealmOrUpdate(event);
//              }
//            });
//
//            if (!SharedPreferencesUtils.getFirebaseEmail(AddActivity_old.this).equals("")) {
//              FirebaseUtils
//                  .addToFirebase(mDatabaseReference, event, AddActivity_old.this, event.getId());
//            }
//
//            finish();
//
//          }
//        }
//
//      }
//    });
//
//    mImageView = (ImageView) findViewById(R.id.event_image);
//    ImageView clickImage = (ImageView) findViewById(R.id.choose_image);
//    clickImage.setOnClickListener(new OnClickListener() {
//      @Override
//      public void onClick(View view) {
//        try {
//          if (ActivityCompat
//              .checkSelfPermission(AddActivity_old.this, Manifest.permission.READ_EXTERNAL_STORAGE)
//              != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(AddActivity_old.this,
//                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_PHOTO_CUSTOM);
//          }
//        } catch (Exception e) {
//          e.printStackTrace();
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity_old.this);
//        builder.setTitle(getString(R.string.add_activity_dialog_title));
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialogInterface, int which) {
//            switch (which) {
//              case 0:
//                CropImage.startPickImageActivity(AddActivity_old.this);
//                break;
//              case 1:
//                Intent intent = new Intent(AddActivity_old.this, GalleryActivity_old.class);
//                startActivityForResult(intent, PICK_PHOTO_GALLERY);
//                break;
//            }
//          }
//        });
//        builder.show();
//      }
//    });
//  }
//
//  private void setUpFireBase() {
//    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
//  }
//
//  private void setUpSpinner() {
//    spinner = (Spinner) findViewById(R.id.spinner_repeat);
//    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//        R.array.add_activity_repeat, R.layout.support_simple_spinner_dropdown_item);
//    spinner.setAdapter(adapter);
//  }
//
//  @Override
//  @SuppressLint("NewApi")
//  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
//        && resultCode == Activity.RESULT_OK) {
//      Uri tempImageUri = CropImage.getPickImageResultUri(this, data);
//      imageUri = tempImageUri;
//
//      // For API >= 23 we need to check specifically that we have permissions to read external storage.
//      if (VERSION.SDK_INT >= 23) {
//        if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
//          // request permissions and handle the result in onRequestPermissionsResult()
//          imageUri = tempImageUri;
//          requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//              CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
//        } else {
//          // no permissions required or already grunted, can start crop image activity
//          startCropImageActivity(imageUri);
//        }
//      } else {
//        startCropImageActivity(imageUri);
//      }
//
//      File file = null;
//      if (!imageUri.toString().equals("")) {
//        file = new File(tempImageUri.getPath());
//      }
//
//      if (file != null) {
//        Picasso.with(this)
//            .load(file)
//            .fit()
//            .centerCrop()
//            .into(mImageView);
//      }
//
//    }
//
//    if (requestCode == PICK_PHOTO_CUSTOM && resultCode == Activity.RESULT_OK) {
//      if (data == null) {
//        return;
//      } else {
//        imageUri = data.getData();
//        imageID = 0;
//
//        CropImage.activity(imageUri)
//            .setGuidelines(CropImageView.Guidelines.ON)
//            .start(this);
//
//      }
//    }
//
//    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//      CropImage.ActivityResult result = CropImage.getActivityResult(data);
//      if (resultCode == RESULT_OK) {
//        imageUri = result.getUri();
//        savefile(imageUri);
//
//        File file = new File(imageUri.getPath());
//
//        Picasso.with(this)
//            .load(file)
//            .fit()
//            .centerCrop()
//            .into(mImageView);
//
//      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//        Exception error = result.getError();
//        error.printStackTrace();
//      }
//    }
//
//  }
//
//  public void onRequestPermissionsResult(int requestCode, String permissions[],
//      int[] grantResults) {
//    if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
//      if (imageUri != null && grantResults.length > 0
//          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        // required permissions granted, start crop image activity
//        startCropImageActivity(imageUri);
//      } else {
//        Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG)
//            .show();
//      }
//    }
//  }
//
//
//  public void setUpRealm() {
//    Realm.init(this);
//    RealmConfiguration config = new RealmConfiguration.Builder()
//        .schemaVersion(2)
//        .migration(new Migration())
//        .build();
//    realm = Realm.getInstance(config);
//  }
//
//  public void receiveEventType() {
//    Intent intent = getIntent();
//    mode = intent.getStringExtra("mode");
//    if (mode == null) {
//      mode = "normal";
//    }
//    id = intent.getIntExtra("id", 1);
//    eventType = intent.getStringExtra("Event Type");
//  }
//
//  public boolean validateForm() {
//    if (mNameEditText.getText().toString().equals("")) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_name), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    if (mDateEditText.getText().toString().equals("")) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_date), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    if (mNameEditText.getText().length() > 30) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_name_length), Toast.LENGTH_SHORT)
//          .show();
//      return false;
//    }
//
//    if (imageUri == null && imageID == 0) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_image), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    if (imageUri != null) {
//      if (imageUri.toString().contains("com.google.android.apps.photos")) {
//        Toast.makeText(this, getString(R.string.add_activity_google_photos), Toast.LENGTH_SHORT)
//            .show();
//        return false;
//      }
//    }
//
//    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
//    try {
//      format.parse(mDateEditText.getText().toString());
//    } catch (ParseException e) {
//      Toast.makeText(this, getString(R.string.add_activity_date), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    return true;
//  }
//
//  public boolean validateFormInEditMode() {
//    if (mNameEditText.getText().toString().equals("")) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_name), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    if (mNameEditText.getText().length() > 35) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_name_length), Toast.LENGTH_SHORT)
//          .show();
//      return false;
//    }
//
//    if (mDateEditText.getText().toString().equals("")) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_date), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    if (imageUri == null && imageID == 0 && mEvent.getImage().equals("")
//        && mEvent.getImageID() == 0) {
//      Toast.makeText(this, getString(R.string.add_activity_toast_image), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
//    try {
//      format.parse(mDateEditText.getText().toString());
//    } catch (ParseException e) {
//      Toast.makeText(this, getString(R.string.add_activity_date), Toast.LENGTH_SHORT).show();
//      return false;
//    }
//
//    return true;
//  }
//
//  @Override
//  protected void onNewIntent(Intent intent) {
//    super.onNewIntent(intent);
//    imageID = intent.getIntExtra("imageID", 0);
//    if (imageID != 0) {
//      imageUri = null;
//      Picasso.with(getApplicationContext()).load(imageID).resize(0, 700).into(mImageView);
//    }
//  }
//
//  public void setUpIfEdit() {
//    if (mode.equals("edit")) {
//      mEvent = realm.where(Event.class).equalTo("id", id).findFirst();
//      mNameEditText.setText(mEvent.getName());
//      mDateEditText.setText(mEvent.getDate());
//      mDescritptionEditText.setText(mEvent.getDescription());
//      if (mEvent.getImageID() != 0) {
//        Picasso.with(this).load(mEvent.getImageID()).resize(0, 700).into(mImageView);
//      } else {
//        Uri uri = Uri.parse(mEvent.getImage());
//        File file = new File(uri.getPath());
//        Picasso.with(this)
//            .load(file)
//            .resize(0, 700)
//            .into(mImageView);
//      }
//      if (mEvent.hasAlarm()) {
//        String date =
//            mEvent.getYear() + "-" + ((mEvent.getMonth() < 10) ? "0" + mEvent.getMonth()
//                : mEvent.getMonth()) + "-" + mEvent.getDay() + " " + mEvent
//                .getHour() + ":" + ((mEvent.getMinute() < 10) ? "0" + mEvent.getMinute()
//                : mEvent.getMinute());
//        mReminderDateEditText.setText(date);
//        mReminderTextEditText.setText(mEvent.getNotificationText());
//        yearNotification = mEvent.getYear();
//        monthNotification = mEvent.getMonth();
//        dayNotification = mEvent.getDay();
//        hourNotification = mEvent.getHour();
//        minuteNotification = mEvent.getMinute();
//      }
//      spinner.setSelection(Integer.parseInt(mEvent.getRepeat()));
//      mAddButton.setText(R.string.add_activity_button_title);
//      getSupportActionBar().setTitle(R.string.add_activity_toolbar_title);
//    }
//  }
//
//  public void displayAd() {
//    if (isNetworkAvailable()) {
//      final AdView mAdView = (AdView) findViewById(R.id.adView);
//      mAdView.setVisibility(View.VISIBLE);
//      final AdRequest request = new AdRequest.Builder().build();
//
//      Handler handler = new Handler();
//      handler.postDelayed(new Runnable() {
//        @Override
//        public void run() {
//          mAdView.loadAd(request);
//        }
//      }, 500);
//    }
//  }
//
//  public void setUpDialogOptions() {
//    options = new String[2];
//    options[0] = getString(R.string.add_activity_dialog_option_custom);
//    options[1] = getString(R.string.add_activity_dialog_option_gallery);
//  }
//
//  private void startCropImageActivity(Uri imageUri) {
//    CropImage.activity(imageUri)
//        .start(this);
//  }
//
//  void savefile(Uri sourceuri) {
//
//    File folder = new File(Environment.getExternalStorageDirectory() + "/croppedImages");
//    boolean success = true;
//    if (!folder.exists()) {
//      success = folder.mkdir();
//    }
//
//    String sourceFilename = sourceuri.getPath();
//    String destinationFilename =
//        android.os.Environment.getExternalStorageDirectory().getPath() + File.separatorChar
//            + "croppedImages/" + sourceuri.getLastPathSegment();
//
//    BufferedInputStream bis = null;
//    BufferedOutputStream bos = null;
//
//    try {
//      bis = new BufferedInputStream(new FileInputStream(sourceFilename));
//      bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
//      byte[] buf = new byte[1024];
//      bis.read(buf);
//      do {
//        bos.write(buf);
//      } while (bis.read(buf) != -1);
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally {
//      try {
//        if (bis != null) {
//          bis.close();
//        }
//        if (bos != null) {
//          bos.close();
//        }
//        imageUri = Uri.parse(destinationFilename);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//  }
//
//  public void getSharedPref() {
//    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//    IsWithoutAds = mSharedPreferences.getBoolean("ads", false);
//  }
//
//  private boolean isNetworkAvailable() {
//    ConnectivityManager connectivityManager
//        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//  }
//
//  private boolean anyFieldIsNotEmpty() {
//    if (!mNameEditText.getText().toString().equals("") ||
//        !mDateEditText.getText().toString().equals("") ||
//        !mDescritptionEditText.getText().toString().equals("") ||
//        !mReminderDateEditText.getText().toString().equals("") ||
//        !mReminderTextEditText.getText().toString().equals("") ||
//        imageID != 0 || imageUri != null) {
//      return true;
//    } else {
//      return false;
//    }
//  }

}
