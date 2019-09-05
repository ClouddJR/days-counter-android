package com.arkadiusz.dayscounter.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.arkadiusz.dayscounter.ui.base.BaseViewModelFactory

object ExtensionUtils {

    inline fun <reified T : ViewModel> Fragment.getViewModel(
            context: Fragment, noinline creator: (() -> T)? = null): T {
        return if (creator == null) {
            ViewModelProviders.of(context).get(T::class.java)
        } else {
            ViewModelProviders.of(context, BaseViewModelFactory<T>(creator)).get(T::class.java)
        }
    }

    inline fun <reified T : ViewModel> Fragment.getViewModel(
            context: FragmentActivity, noinline creator: (() -> T)? = null): T {
        return if (creator == null) {
            ViewModelProviders.of(context).get(T::class.java)
        } else {
            ViewModelProviders.of(context, BaseViewModelFactory<T>(creator)).get(T::class.java)
        }
    }

    inline fun <reified T : ViewModel> FragmentActivity.getViewModel(
            context: Fragment, noinline creator: (() -> T)? = null): T {
        return if (creator == null) {
            ViewModelProviders.of(context).get(T::class.java)
        } else {
            ViewModelProviders.of(context, BaseViewModelFactory<T>(creator)).get(T::class.java)
        }
    }

    inline fun <reified T : ViewModel> FragmentActivity.getViewModel(
            context: FragmentActivity, noinline creator: (() -> T)? = null): T {
        return if (creator == null) {
            ViewModelProviders.of(context).get(T::class.java)
        } else {
            ViewModelProviders.of(context, BaseViewModelFactory<T>(creator)).get(T::class.java)
        }
    }
}