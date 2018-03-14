package com.arkadiusz.dayscounter.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkadiusz.dayscounter.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.single_image_gallery.view.*

/**
 * Created by arkadiusz on 11.03.18
 */

class GalleryAdapter(val imagesList: IntArray, val context: Context) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_image_gallery, parent)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.displayImage(position)

    override fun getItemCount(): Int = imagesList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun displayImage(position: Int) {
            Picasso.with(context).load(imagesList[position]).resize(0, 300).into(itemView.galleryImage)
        }
    }
}