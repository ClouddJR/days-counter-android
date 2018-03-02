package com.arkadiusz.dayscounter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import com.arkadiusz.dayscounter.R;

/**
 * Created by Arkadiusz on 06.01.2017.
 */

public class GalleryViewHolder extends RecyclerView.ViewHolder {

  public ImageView mImageView;

  public GalleryViewHolder(View itemView) {
    super(itemView);
    mImageView = (ImageView) itemView.findViewById(R.id.gallery_image);
  }
}
