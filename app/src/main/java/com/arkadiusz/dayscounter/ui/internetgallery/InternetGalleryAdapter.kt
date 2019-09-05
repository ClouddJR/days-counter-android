package com.arkadiusz.dayscounter.ui.internetgallery

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.single_internet_image_gallery.view.*


class InternetGalleryAdapter(val listener: ImageClickListener) :
        PagedListAdapter<Image, InternetGalleryAdapter.ImageViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_internet_image_gallery, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) = holder.bind(position)

    override fun getItemViewType(position: Int) = position

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {
            getItem(position)?.let { image ->

                Glide.with(itemView.context)
                        .load(image.imageUrls.smallUrl)
                        .into(itemView.photoIV)

                val userProfileUrl = if (Build.VERSION.SDK_INT >= 24) {
                    Html.fromHtml(itemView.context.getString(R.string.author_link) +
                            "<a href ='${image.imageAuthor.userLinks.profileUrl}?utm_source=Days Counter&" +
                            "utm_medium=referral'>" +
                            image.imageAuthor.fullName,
                            Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(itemView.context.getString(R.string.author_link) +
                            "<a href ='${image.imageAuthor.userLinks.profileUrl}?utm_source=Days Counter&" +
                            "utm_medium=referral'>" +
                            image.imageAuthor.fullName)
                }
                itemView.authorTV.text = userProfileUrl
                itemView.authorTV.isClickable = true
                itemView.authorTV.movementMethod = LinkMovementMethod.getInstance()

                itemView.photoIV.setOnClickListener {
                    listener.onImageClick(image)
                }
            }
        }
    }

    interface ImageClickListener {
        fun onImageClick(image: Image)
    }


    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<Image>() {

            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.imageId == newItem.imageId
            }

            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem == newItem
            }
        }
    }

}