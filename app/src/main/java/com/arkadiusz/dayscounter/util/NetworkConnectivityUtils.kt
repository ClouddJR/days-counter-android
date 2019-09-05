package com.arkadiusz.dayscounter.util

import android.content.Context
import android.net.ConnectivityManager

object NetworkConnectivityUtils {

    fun isNetworkEnabled(context: Context?): Boolean {
        context?.let {
            val connectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo != null && connectivityManager
                    .activeNetworkInfo.isConnected
        }

        return false
    }
}