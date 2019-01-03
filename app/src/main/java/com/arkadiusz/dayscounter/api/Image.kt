package com.arkadiusz.dayscounter.api

import com.google.gson.annotations.SerializedName

data class Image(
        @SerializedName("id") val imageId: String,
        @SerializedName("user") val imageAuthor: ImageAuthor,
        @SerializedName("urls") val imageUrls: ImageUrls,
        var nextPage: Long = 1
)