package com.arkadiusz.dayscounter.activities;

import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.database.FirebaseTempObject;
import com.arkadiusz.dayscounter.fragments.FutureFragment;
import com.arkadiusz.dayscounter.fragments.PastFragment;
import com.arkadiusz.dayscounter.model.Migration;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.utils.FirebaseUtils;
import com.arkadiusz.dayscounter.utils.IabHelper;
import com.arkadiusz.dayscounter.utils.IabHelper.IabAsyncInProgressException;
import com.arkadiusz.dayscounter.utils.IabResult;
import com.arkadiusz.dayscounter.utils.Inventory;
import com.arkadiusz.dayscounter.utils.Purchase;
import com.arkadiusz.dayscounter.utils.SharedPreferencesUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private Toolbar toolbar;
  private TabLayout tabLayout;
  private ViewPager viewPager;
  private ViewPagerAdapter adapter;
  private Realm realm;
  private RealmConfiguration config;
  private SharedPreferences mSharedPreferences;
  private IabHelper mHelper;
  private static final int REQUEST_CODE_EMAIL = 1;
  private DatabaseReference mDatabaseReference;
  public static String userMail;
  final List<Event> allEventsInFirebase = new ArrayList<>();
  String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAljgvqjNFwvk8KX5N1yfCAb+dOtaN5vYiERZ4JwpJfQKV2IQEQ04H9+mZE6FyoH6g5LyFAuFY28eJCoNWNsQ4rjDgU33Ta4ZXjxdLCPyw5rwkWU8LoJIxjaf9Ftau62d2SvkcDFDFSV70RUyU6UxlDeDXblZgD799A1zwMPCXLVeKqTnK7GqXsGo48KfBsbMgKsn7gKWuTNSO0RK3UTH8TkzKkjFF97QSBRN6WLWcTHNWzttb+BIMZWZv5H6TIySo/d5MKwKPPojLRDRpepZsjGrGD9td93SN+4X/kFW9t0/S+3Gl1tQWnzDCVhLFhK7aR0hDqFYPMLQGDqcHNZSpbwIDAQAB";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (SharedPreferencesUtils.isBlackTheme(this)) {
      setTheme(R.style.BlackMain);
    }

    setContentView(R.layout.activity_main_black);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(R.string.app_name);

    showChangelogDialog();
    setUpRealm();
    setUpTabLayout();
    setUpFAB();
    setUpSharedPreferences();
    checkForPurchases();

    logEvents();

  }


  private void logEvents() {
    final RealmResults<Event> entireLocalDatabase = realm.where(Event.class).findAll();
    for (Event event : entireLocalDatabase) {
      Log.d("event", event.toString());
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
      userMail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

      if (userMail != null) {
        formatMail();

        if (userMail.equals(SharedPreferencesUtils.getFirebaseEmail(this))) {
          Toast.makeText(this,
              getString(R.string.main_activity_sync_same_email) + " " + SharedPreferencesUtils
                  .getFirebaseEmail(this), Toast.LENGTH_SHORT).show();
          return;
        }

        if (!SharedPreferencesUtils.getFirebaseEmail(this).equals("") && !userMail
            .equals(SharedPreferencesUtils.getFirebaseEmail(this))) {
          FirebaseUtils.deletePreviousMail(SharedPreferencesUtils.getFirebaseEmail(this));
        }

        SharedPreferencesUtils.setFirebaseEmail(this, userMail);
        processFirebase();
      }
    }
  }


  private void setupViewPager(ViewPager viewPager) {
    adapter.addFragment(new FutureFragment(), getString(R.string.main_activity_right_tab));
    adapter.addFragment(new PastFragment(), getString(R.string.main_activity_left_tab));
    viewPager.setAdapter(adapter);
  }

  private class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    ViewPagerAdapter(FragmentManager manager) {
      super(manager);
    }


    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
      return mFragmentList.size();
    }

    void addFragment(Fragment fragment, String title) {
      mFragmentList.add(fragment);
      mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mFragmentTitleList.get(position);
    }
  }


  public void setUpTabLayout() {
    viewPager = (ViewPager) findViewById(R.id.viewpager);
    adapter = new ViewPagerAdapter(getSupportFragmentManager());
    setupViewPager(viewPager);
    tabLayout = (TabLayout) findViewById(R.id.tabs);
    assert tabLayout != null;
    tabLayout.setupWithViewPager(viewPager);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_contact) {
      Intent i = new Intent(Intent.ACTION_SEND);
      i.setType("message/rfc822");
      i.putExtra(Intent.EXTRA_EMAIL, new String[]{"arekchmura@gmail.com"});
      i.putExtra(Intent.EXTRA_SUBJECT, "Days Counter app");
      i.putExtra(Intent.EXTRA_TEXT, "");
      try {
        startActivity(Intent.createChooser(i, "Send mail..."));
      } catch (android.content.ActivityNotFoundException ex) {
        Toast.makeText(MainActivity.this, "There are no email clients installed.",
            Toast.LENGTH_SHORT).show();
      }
    } else if (id == R.id.action_sort_date_desc) {
      SharedPreferences.Editor editor = mSharedPreferences.edit();
      editor.putString("sort_type", "date_desc");
      editor.apply();
      FutureFragment fragmentFuture = (FutureFragment) adapter.getItem(0);
      PastFragment fragmentPast = (PastFragment) adapter.getItem(1);
      fragmentFuture.refreshData();
      fragmentPast.refreshData();
    } else if (id == R.id.action_sort_date_asc) {
      SharedPreferences.Editor editor = mSharedPreferences.edit();
      editor.putString("sort_type", "date_asc");
      editor.apply();
      FutureFragment fragmentFuture = (FutureFragment) adapter.getItem(0);
      PastFragment fragmentPast = (PastFragment) adapter.getItem(1);
      fragmentFuture.refreshData();
      fragmentPast.refreshData();
    } else if (id == R.id.action_sort_order) {
      SharedPreferences.Editor editor = mSharedPreferences.edit();
      editor.putString("sort_type", "date_order");
      editor.apply();
      FutureFragment fragmentFuture = (FutureFragment) adapter.getItem(0);
      PastFragment fragmentPast = (PastFragment) adapter.getItem(1);
      fragmentFuture.refreshData();
      fragmentPast.refreshData();
    } else if (id == R.id.action_remove_ads) {
      try {
        mHelper.launchPurchaseFlow(this, "1", 10001,
            mPurchaseFinishedListener, "");
      } catch (IabAsyncInProgressException e) {
        e.printStackTrace();
      }
    } else if (id == R.id.action_syncing) {
      if (!FirebaseUtils.isNetworkEnabled(this)) {
        Toast.makeText(this, getString(R.string.main_activity_sync_no_connection),
            Toast.LENGTH_SHORT).show();
        return false;
      }
      getEmailAddress();
    } else if (id == R.id.action_black_theme) {
      if (SharedPreferencesUtils.isBlackTheme(this)) {
        SharedPreferencesUtils.setBlackTheme(this, "white");
        finish();
        startActivity(getIntent());
      } else {
        SharedPreferencesUtils.setBlackTheme(this, "black");
        finish();
        startActivity(getIntent());
      }
    }

    return super.onOptionsItemSelected(item);
  }


  public void setUpRealm() {
    Realm.init(getApplicationContext());
    config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();

    realm = Realm.getInstance(config);
  }


  public void setUpFAB() {
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    assert fab != null;
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (adapter.getItem(viewPager.getCurrentItem()) instanceof PastFragment) {
          String eventType = "past";
          Intent intent = new Intent(MainActivity.this, AddActivity.class);
          intent.putExtra("Event Type", eventType);
          /*if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            String transitionName = getString(R.string.transition_fab);
            ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(MainActivity.this,
                    view,
                    transitionName);
            ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
          } else {*/
          startActivity(intent);
          //}
        }
        if (adapter.getItem(viewPager.getCurrentItem()) instanceof FutureFragment) {
          String eventType = "future";
          Intent intent = new Intent(MainActivity.this, AddActivity.class);
          intent.putExtra("Event Type", eventType);
          /*if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            String transitionName = getString(R.string.transition_fab);
            ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(MainActivity.this,
                    view,
                    transitionName);
            ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
          } else {*/
          startActivity(intent);
          //}
        }
      }
    });

  }

  public void setUpSharedPreferences() {
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mHelper != null) {
      try {
        mHelper.dispose();
      } catch (IabAsyncInProgressException e) {
        e.printStackTrace();
      }
    }
    mHelper = null;
  }


  IabHelper.QueryInventoryFinishedListener mGotInventoryListener
      = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

      if (result.isFailure()) {
      } else {
        Purchase purchase = inventory.getPurchase("1");
        if (purchase != null) {
          SharedPreferences.Editor editor = mSharedPreferences.edit();
          editor.putBoolean("ads", true);
          editor.apply();
        }
      }
    }
  };


  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
      = new IabHelper.OnIabPurchaseFinishedListener() {
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
      if (result.isFailure()) {
        Log.d("IabHelper", "Error getting informations about purchases");
      } else if (purchase.getSku().equals("1")) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ads", true);
        editor.apply();
      }
    }
  };

  private void getEmailAddress() {
    try {
      Intent intent = AccountPicker.newChooseAccountIntent(null, null,
          new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
      startActivityForResult(intent, REQUEST_CODE_EMAIL);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(this, getString(R.string.main_activity_sync_toast), Toast.LENGTH_SHORT).show();
    }
  }

  private void processFirebase() {

    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    mDatabaseReference.child(userMail).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
          allEventsInFirebase.add(postSnapshot.getValue(Event.class));
        }
        addLocalEventsToFirebase();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }

    });


  }


  private void addLocalEventsToFirebase() {
    final RealmResults<Event> entireLocalDatabase = realm.where(Event.class).findAll();

    int lastIdInFirebase;
    if (allEventsInFirebase.size() == 0) {
      lastIdInFirebase = 0;
    } else {
      lastIdInFirebase = allEventsInFirebase.get(allEventsInFirebase.size() - 1).getId();
    }

    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    for (final Event event : entireLocalDatabase) {
      if (!SharedPreferencesUtils.getFirebaseEmail(this).equals("")) {
        if (FirebaseUtils.isUnique(allEventsInFirebase, event)) {
          lastIdInFirebase++;
          FirebaseUtils.addToFirebase(mDatabaseReference, event, this, lastIdInFirebase);
        }
      }
    }

    realm.executeTransaction(new Transaction() {
      @Override
      public void execute(Realm realm) {
        realm.deleteAll();
      }
    });

    mDatabaseReference.child(userMail).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
          realm.executeTransaction(new Transaction() {
            @Override
            public void execute(Realm realm) {
              FirebaseTempObject tempObject = postSnapshot.getValue(FirebaseTempObject.class);
              realm.copyToRealm(FirebaseUtils.parseToEventObject(tempObject));
            }
          });
        }

        FutureFragment futureFragment = (FutureFragment) adapter.getItem(0);
        PastFragment pastFragment = (PastFragment) adapter.getItem(1);

        futureFragment.refreshData();
        pastFragment.refreshData();

        SharedPreferencesUtils.setFirebaseSynced(MainActivity.this, "true");
        Toast.makeText(MainActivity.this,
            MainActivity.this.getString(R.string.main_activity_sync_first_time) + " "
                + SharedPreferencesUtils.getFirebaseEmail(MainActivity.this), Toast.LENGTH_LONG)
            .show();

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }

    });


  }

  private void checkForPurchases() {
    mHelper = new IabHelper(this, base64EncodedPublicKey);
    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh no, there was a problem.
        } else {
          try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
          } catch (IabAsyncInProgressException e) {
            e.printStackTrace();
          }
        }
      }
    });

  }

  private static void formatMail() {
    StringBuilder mailBuilder = new StringBuilder();
    for (char c : userMail.toCharArray()) {
      if ((c != (char) 46) && (c != (char) 35) && (c != (char) 36) && (c != (char) 91) && (c
          != (char) 93)) {
        mailBuilder.append(c);
      }
    }
    userMail = mailBuilder.toString();
  }

  private void showChangelogDialog() {
    AlertDialog.Builder dialog;
    if (!SharedPreferencesUtils.isDialogSeen(this)) {
      if (SharedPreferencesUtils.isBlackTheme(this)) {
        dialog = new AlertDialog.Builder(this, R.style.BlackAlertDialog);
      } else {
        dialog = new AlertDialog.Builder(this);
      }
      dialog.setTitle(getString(R.string.changelog_dialog_title));
      dialog.setMessage(getString(R.string.changelog_dialog_content));
      dialog.setCancelable(false);
      dialog.setPositiveButton("OK", new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          dialogInterface.dismiss();
        }
      });
      dialog.show();
      SharedPreferencesUtils.setDialogInfoSeen(this);
    }
  }

}
