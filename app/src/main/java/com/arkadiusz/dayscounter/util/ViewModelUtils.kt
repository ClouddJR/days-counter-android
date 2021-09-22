package com.arkadiusz.dayscounter.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkadiusz.dayscounter.ui.base.BaseViewModelFactory

object ViewModelUtils {

    inline fun <reified T : ViewModel> getViewModel(
        owner: ViewModelStoreOwner, noinline creator: (() -> T)? = null
    ): T {
        return if (creator == null) {
            ViewModelProvider(owner).get(T::class.java)
        } else {
            ViewModelProvider(owner, BaseViewModelFactory(creator)).get(T::class.java)
        }
    }
}