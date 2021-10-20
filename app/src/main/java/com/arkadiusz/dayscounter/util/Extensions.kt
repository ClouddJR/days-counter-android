package com.arkadiusz.dayscounter.util

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

inline fun Spinner.doOnSelected(
    crossinline onSelected: (view: View?) -> Unit
) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            onSelected.invoke(view)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // nop
        }
    }
}