package com.arkadiusz.dayscounter.ui.main

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.events.FutureEventsFragment
import com.arkadiusz.dayscounter.ui.events.PastEventsFragment

class ViewPagerAdapter(
    private val context: Context, manager: FragmentManager,
    private val defaultFragment: String
) : FragmentPagerAdapter(manager) {

    override fun getItem(position: Int) = when (position) {
        0 -> if (defaultFragment == context.getString(R.string.main_activity_left_tab))
            PastEventsFragment() else
            FutureEventsFragment()
        1 -> if (defaultFragment == context.getString(R.string.main_activity_left_tab))
            FutureEventsFragment() else
            PastEventsFragment()
        else -> throw IllegalStateException("Unexpected position $position")
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> if (defaultFragment == context.getString(R.string.main_activity_left_tab))
            context.getString(R.string.main_activity_left_tab) else
            context.getString(R.string.main_activity_right_tab)
        1 -> if (defaultFragment == context.getString(R.string.main_activity_left_tab))
            context.getString(R.string.main_activity_right_tab) else
            context.getString(R.string.main_activity_left_tab)
        else -> throw IllegalStateException("Unexpected position $position")
    }
}