package com.arkadiusz.dayscounter.util

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData

inline fun Spinner.doOnSelected(
    crossinline onSelected: (view: View?, position: Int) -> Unit
) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            onSelected.invoke(view, position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // nop
        }
    }
}

fun Spinner.indexOfFirst(predicate: (Any) -> Boolean): Int {
    for (index in 0 until count) {
        if (predicate(getItemAtPosition(index))) {
            return index
        }
    }
    return -1
}


fun <T> ComponentActivity.observe(liveData: LiveData<T>, observer: (data: T) -> Unit) {
    liveData.observe(this) {
        observer.invoke(it)
    }
}