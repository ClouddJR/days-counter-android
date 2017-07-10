package com.arkadiusz.dayscounter.Activities;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.arkadiusz.dayscounter.Fragments.FutureFragment;
import com.arkadiusz.dayscounter.Fragments.PastFragment;
import com.arkadiusz.dayscounter.Model.Migration;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.Utils.IabHelper;
import com.arkadiusz.dayscounter.Utils.IabHelper.IabAsyncInProgressException;
import com.arkadiusz.dayscounter.Utils.IabResult;
import com.arkadiusz.dayscounter.Utils.Inventory;
import com.arkadiusz.dayscounter.Utils.Purchase;
import io.realm.Realm;
import io.realm.RealmConfiguration;
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
  String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAljgvqjNFwvk8KX5N1yfCAb+dOtaN5vYiERZ4JwpJfQKV2IQEQ04H9+mZE6FyoH6g5LyFAuFY28eJCoNWNsQ4rjDgU33Ta4ZXjxdLCPyw5rwkWU8LoJIxjaf9Ftau62d2SvkcDFDFSV70RUyU6UxlDeDXblZgD799A1zwMPCXLVeKqTnK7GqXsGo48KfBsbMgKsn7gKWuTNSO0RK3UTH8TkzKkjFF97QSBRN6WLWcTHNWzttb+BIMZWZv5H6TIySo/d5MKwKPPojLRDRpepZsjGrGD9td93SN+4X/kFW9t0/S+3Gl1tQWnzDCVhLFhK7aR0hDqFYPMLQGDqcHNZSpbwIDAQAB";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(R.string.app_name);

    setUpRealm();
    setUpTabLayout();
    setUpFAB();
    setUpSharedPreferences();

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

  private void setupViewPager(ViewPager viewPager) {
    adapter.addFragment(new FutureFragment(), getString(R.string.main_activity_right_tab));
    adapter.addFragment(new PastFragment(), getString(R.string.main_activity_left_tab));
    viewPager.setAdapter(adapter);
  }

  class ViewPagerAdapter extends FragmentPagerAdapter {

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
    public void onQueryInventoryFinished(IabResult result,
        Inventory inventory) {

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
      } else if (purchase.getSku().equals("1")) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ads", true);
        editor.apply();
      }
    }
  };


}
