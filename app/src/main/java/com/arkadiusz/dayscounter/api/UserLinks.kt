package com.arkadiusz.dayscounter.api

import com.google.gson.annotations.SerializedName

data class UserLinks(
        @SerializedName("html") val profileUrl: String
)