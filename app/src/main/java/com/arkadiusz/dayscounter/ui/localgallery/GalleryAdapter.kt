package com.arkadiusz.dayscounter.ui.localgallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arkadiusz.dayscounter.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.single_image_gallery.view.*

class GalleryAdapter(val imagesList: IntArray, val context: Context) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_image_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.displayImage(position)

    override fun getItemCount(): Int = imagesList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun displayImage(position: Int) {
            Glide.with(context)
                .load(imagesList[position])
                .into(itemView.galleryImage)
        }
    }
}