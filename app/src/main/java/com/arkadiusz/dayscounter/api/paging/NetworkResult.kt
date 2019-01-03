package com.arkadiusz.dayscounter.api.paging


enum class Status {
    EMPTY, NOT_EMPTY
}

data class NetworkResult(
        val status: Status
)