package com.arkadiusz.dayscounter.data.model.unsplash

import com.google.gson.annotations.SerializedName

data class UserLinks(
        @SerializedName("html") val profileUrl: String
)