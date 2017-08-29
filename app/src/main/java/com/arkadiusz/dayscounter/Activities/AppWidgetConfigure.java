package com.arkadiusz.dayscounter.Activities;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.TextView;
import com.arkadiusz.dayscounter.Adapters.WidgetConfigureAdapter;
import com.arkadiusz.dayscounter.Database.Event;
import com.arkadiusz.dayscounter.Model.Migration;
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.Utils.FirebaseUtils;
import com.arkadiusz.dayscounter.Utils.SharedPreferencesUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

public class AppWidgetConfigure extends AppCompatActivity {

  private int mAppWidgetId;
  private Realm realm;
  private RealmConfiguration config;
  RealmResults<Event> results;
  private DatabaseReference mDatabaseReference;

  private int mSelectedColor;
  private TextView textView;
  private Button mImageView;
  private Switch mSwitch;
  private boolean isCheckedDays = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (SharedPreferencesUtils.isBlackTheme(this)) {
      setContentView(R.layout.activity_app_widget_configure_black);
    } else {
      setContentView(R.layout.activity_app_widget_configure);
    }
    setResult(RESULT_CANCELED);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      mAppWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    Realm.init(getApplicationContext());
    config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);
    results = realm.where(Event.class).findAll();

    final WidgetConfigureAdapter adapter = new WidgetConfigureAdapter(getApplicationContext(),
        results);

    ListView listView = (ListView) findViewById(R.id.listView);
    listView.setAdapter(adapter);

    if (ActivityCompat
        .checkSelfPermission(AppWidgetConfigure.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(AppWidgetConfigure.this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    mSelectedColor = ContextCompat.getColor(this, R.color.black);

    textView = (TextView) findViewById(R.id.text);
    mImageView = (Button) findViewById(R.id.color_image);

    int[] mColors = getResources().getIntArray(R.array.rainbow);

    final ColorPickerDialog dialog = ColorPickerDialog
        .newInstance(R.string.widget_configuration_dialog_title,
            mColors,
            mSelectedColor,
            5, // Number of columns
            ColorPickerDialog.SIZE_SMALL);

    dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

      @Override
      public void onColorSelected(int color) {
        mSelectedColor = color;
        mImageView.setBackgroundColor(mSelectedColor);
      }

    });

    mImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.show(getFragmentManager(), "color_dialog_test");
      }
    });

    mSwitch = (Switch) findViewById(R.id.switch1);
    mSwitch.setChecked(false);
    mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          isCheckedDays = true;
        } else {
          isCheckedDays = false;
        }
      }
    });

    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView text = (TextView) view;
        String name = text.getText().toString();

        results = realm.where(Event.class).equalTo("name", name).findAll();
        Event mEvent;
        if (results.size() != 0) {
          mEvent = results.first();
          final Event event = new Event();

          event.setId(mEvent.getId());
          event.setImageID(mEvent.getImageID());
          event.setImage(mEvent.getImage());
          event.setType(mEvent.getType());
          event.setDate(mEvent.getDate());
          event.setDescription(mEvent.getDescription());
          event.setName(mEvent.getName());
          event.setWidgetID(mAppWidgetId);
          event.setColor(mSelectedColor);
          event.setYear(mEvent.getYear());
          event.setMonth(mEvent.getMonth());
          event.setDay(mEvent.getDay());
          event.setHour(mEvent.getHour());
          event.setMinute(mEvent.getMinute());
          event.setRepeat(mEvent.getRepeat());
          event.setNotificationText(mEvent.getNotificationText());
          if (mEvent.hasAlarm()) {
            event.setHasAlarm(true);
          } else {
            event.setHasAlarm(false);
          }
          if (isCheckedDays) {
            event.setOnlyDays(true);
          } else {
            event.setOnlyDays(false);
          }

          realm.executeTransaction(new Transaction() {
            @Override
            public void execute(Realm realm) {
              realm.copyToRealmOrUpdate(event);
            }
          });

          mDatabaseReference = FirebaseDatabase.getInstance().getReference();

          if (!SharedPreferencesUtils.getFirebaseEmail(AppWidgetConfigure.this).equals("")) {
            FirebaseUtils
                .addToFirebase(mDatabaseReference, event, AppWidgetConfigure.this, event.getId());
          }

          AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());

          RemoteViews views = new RemoteViews(getBaseContext().getPackageName(),
              R.layout.appwidget);
          appWidgetManager.updateAppWidget(mAppWidgetId, views);

          Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
              getApplicationContext(), AppWidgetProvider.class);
          intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mAppWidgetId});
          sendBroadcast(intent);

          Intent resultValue = new Intent();
          resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
          setResult(RESULT_OK, resultValue);
          finish();
        }


      }
    });

  }

}
