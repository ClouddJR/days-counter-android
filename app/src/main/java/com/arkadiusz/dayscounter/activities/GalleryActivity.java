package com.arkadiusz.dayscounter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.adapters.GalleryAdapter;
import com.arkadiusz.dayscounter.model.RecyclerItemClickListener;
import com.arkadiusz.dayscounter.model.RecyclerItemClickListener.OnItemClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class GalleryActivity extends AppCompatActivity {

  private RecyclerView mRecyclerView;
  private GalleryAdapter mGalleryAdapter;
  private SharedPreferences mSharedPreferences;
  private int[] mImagesList;
  private boolean IsWithoutAds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gallery);
    setUpImagesList();
    getSupportActionBar().setTitle(getString(R.string.gallery_activity_title));
    setUpRecyclerView();
    getSharedPref();

    if (!IsWithoutAds) {
      displayAd();
    } else {
      adjustMargins();
    }
  }

  public void setUpImagesList() {
    mImagesList = new int[58];
    for (int i = 0; i <= mImagesList.length - 1; i++) {
      mImagesList[i] = getResources().getIdentifier("a" + (i + 1), "drawable", getPackageName());
    }

  }

  public void setUpRecyclerView() {
    mRecyclerView = (RecyclerView) findViewById(R.id.gallery_recycler_view);
    mGalleryAdapter = new GalleryAdapter(getApplicationContext(), mImagesList);
    mRecyclerView
        .setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setAdapter(mGalleryAdapter);
    mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
        mRecyclerView, new OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {
        Intent intent = new Intent(GalleryActivity.this, AddActivity.class);
        intent.putExtra("imageID", mImagesList[position]);
        startActivity(intent);
        finish();
      }

      @Override
      public void onItemLongClick(View view, int position) {

      }
    }));
  }

  public void displayAd() {
    if (isNetworkAvailable()) {
      final AdView mAdView = (AdView) findViewById(R.id.adView2);
      mAdView.setVisibility(View.VISIBLE);
      final AdRequest request = new AdRequest.Builder().build();

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mAdView.loadAd(request);
        }
      });

/*      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        public void run() {
          mAdView.loadAd(request);
        }
      }, 400);*/
    } else {
      adjustMargins();
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

  private void adjustMargins() {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
    params.setMargins(0,0,0,0);
    mRecyclerView.setLayoutParams(params);
  }
}
