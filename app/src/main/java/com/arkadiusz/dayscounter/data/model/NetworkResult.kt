package com.arkadiusz.dayscounter.data.model

enum class Status {
    EMPTY, NOT_EMPTY
}

data class NetworkResult(
    val status: Status
)