package com.arkadiusz.dayscounter.util

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import android.content.Context
import com.arkadiusz.dayscounter.R

object ThemeUtils {

    fun getThemeFromPreferences(isActionBarEnabled: Boolean, context: Context): Int {
        val prefs = defaultPrefs(context)
        val theme = prefs["theme"] ?: "1"

        return if (!isActionBarEnabled) {
            when (theme) {
                "1" -> R.style.AppTheme_NoActionBar_Light_Default
                "2" -> R.style.AppTheme_NoActionBar_Light_Blue
                "3" -> R.style.AppTheme_NoActionBar_Light_Red
                "4" -> R.style.AppTheme_NoActionBar_Light_Green
                "5" -> R.style.AppTheme_NoActionBar_Dark_Green
                "6" -> R.style.AppTheme_NoActionBar_Dark_Purple
                "7" -> R.style.AppTheme_NoActionBar_Dark_Blue
                "8" -> R.style.AppTheme_NoActionBar_Dark_Red
                else -> R.style.AppTheme_NoActionBar_Light_Default
            }
        } else {
            when (theme) {
                "1" -> R.style.AppTheme_Light_Default
                "2" -> R.style.AppTheme_Light_Blue
                "3" -> R.style.AppTheme_Light_Red
                "4" -> R.style.AppTheme_Light_Green
                "5" -> R.style.AppTheme_Dark_Green
                "6" -> R.style.AppTheme_Dark_Purple
                "7" -> R.style.AppTheme_Dark_Blue
                "8" -> R.style.AppTheme_Dark_Red
                else -> R.style.AppTheme_Light_Default
            }
        }
    }
}