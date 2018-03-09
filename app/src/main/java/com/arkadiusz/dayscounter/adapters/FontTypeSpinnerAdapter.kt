package com.arkadiusz.dayscounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.arkadiusz.dayscounter.utils.FontUtils

/**
 * Created by arkadiusz on 09.03.18
 */

class FontTypeSpinnerAdapter(context: Context, resource: Int, list: List<String>) : ArrayAdapter<String>(context, resource, list) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = super.getView(position, convertView, parent) as TextView
        val typeFace = FontUtils.getFontFor(textView.text.toString(), context)
        textView.typeface = typeFace
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = super.getDropDownView(position, convertView, parent) as TextView
        val typeFace = FontUtils.getFontFor(textView.text.toString(), context)
        textView.typeface = typeFace
        return textView
    }
}