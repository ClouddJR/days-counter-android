package com.arkadiusz.dayscounter.ui.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import com.arkadiusz.dayscounter.data.model.Event
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter

/**
 * Created by arkadiusz on 23.03.18
 */

class WidgetConfigureAdapter(val context: Context, private val eventsList: OrderedRealmCollection<Event>) : RealmBaseAdapter<Event>(eventsList),
        ListAdapter {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        lateinit var viewHolder: ViewHolder
        var passedView = convertView
        if (passedView == null) {
            passedView = LayoutInflater.from(parent?.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            viewHolder = ViewHolder()
            viewHolder.evenTitle = passedView?.findViewById(android.R.id.text1)
            passedView.tag = viewHolder
        } else {
            viewHolder = convertView?.tag as ViewHolder
        }

        viewHolder.evenTitle?.text = eventsList[position].name
        return passedView
    }

    private class ViewHolder {
        internal var evenTitle: TextView? = null
    }
}