package com.arkadiusz.dayscounter.data.model.unsplash

import com.google.gson.annotations.SerializedName

data class ImagesResponse(
        @SerializedName("total_pages") val pages: Int,
        @SerializedName("results") val imagesList: List<Image>
)