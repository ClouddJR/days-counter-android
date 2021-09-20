package com.arkadiusz.dayscounter.data.model.unsplash

import com.google.gson.annotations.SerializedName

data class ImageAuthor(
    @SerializedName("username") val userName: String,
    @SerializedName("name") val fullName: String,
    @SerializedName("links") val userLinks: UserLinks
)