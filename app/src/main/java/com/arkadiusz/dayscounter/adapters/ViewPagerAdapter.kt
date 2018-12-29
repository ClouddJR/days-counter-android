package com.arkadiusz.dayscounter.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(manager: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(manager) {

    private val fragmentList = mutableListOf<androidx.fragment.app.Fragment>()
    private val fragmentTitleList = mutableListOf<String>()

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }

    fun addFragment(fragment: androidx.fragment.app.Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }
}