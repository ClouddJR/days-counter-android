package com.arkadiusz.dayscounter.api

import com.google.gson.annotations.SerializedName

data class ImageAuthor(
        @SerializedName("username") val userName: String,
        @SerializedName("links") val userLinks: UserLinks
)