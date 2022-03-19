package com.arkadiusz.dayscounter.util

class MessageWrapper<T>(private val value: T) {

    private var hasBeenHandled = false

    fun get(): T? {
        return if (!hasBeenHandled) {
            hasBeenHandled = true
            value
        } else {
            null
        }
    }
}