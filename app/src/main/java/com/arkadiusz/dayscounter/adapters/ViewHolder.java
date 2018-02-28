package com.arkadiusz.dayscounter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.arkadiusz.dayscounter.R;


public class ViewHolder extends RecyclerView.ViewHolder {

  public ImageView mImageView;
  public TextView mNameTextView;
  public TextView mDaysNumberTextView;

  public ViewHolder(View view) {
    super(view);
    mImageView = (ImageView) view.findViewById(R.id.eventImage);
    mNameTextView = (TextView) view.findViewById(R.id.eventName);
    mDaysNumberTextView = (TextView) view.findViewById(R.id.daysNumber);
  }
}
