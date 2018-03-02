package com.arkadiusz.dayscounter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.arkadiusz.dayscounter.R;
import com.squareup.picasso.Picasso;


/**
 * Created by Arkadiusz on 06.01.2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder> {

  private Context context;
  private int[] mImagesList;

  public GalleryAdapter(Context context, int[] imagesList) {
    this.context = context;
    this.mImagesList = imagesList;
  }

  @Override
  public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.single_image_gallery, parent, false);
    GalleryViewHolder vh = new GalleryViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(GalleryViewHolder holder, int position) {
    Picasso.with(context).load(mImagesList[position]).resize(0, 300).into(holder.mImageView);
  }

  @Override
  public int getItemCount() {
    return mImagesList.length;
  }

}
