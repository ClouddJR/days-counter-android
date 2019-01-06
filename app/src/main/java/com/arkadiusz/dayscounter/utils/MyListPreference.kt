package com.arkadiusz.dayscounter.utils

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

/*  Overriding ListPreference to not show default dialog when click listener is null
    Used when disabling features in settings but still showing premium dialog */

class MyListPreference : ListPreference {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context?) :
            super(context)


    override fun onClick() {
        if (onPreferenceClickListener == null) {
            super.onClick()
        }
    }
}