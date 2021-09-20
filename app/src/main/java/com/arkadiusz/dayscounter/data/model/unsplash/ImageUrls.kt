package com.arkadiusz.dayscounter.data.model.unsplash

import com.google.gson.annotations.SerializedName

data class ImageUrls(
    @SerializedName("raw") val rawUrl: String,
    @SerializedName("full") val fullUrl: String,
    @SerializedName("regular") val regularUrl: String,
    @SerializedName("small") val smallUrl: String
)