package com.arkadiusz.dayscounter.ui.main

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.repository.UserRepository
import com.arkadiusz.dayscounter.util.billing.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val billingRepository: BillingRepository,
) : ViewModel() {

    fun getBillingLifecycleObserver(): LifecycleObserver {
        return billingRepository.getBillingLifecycleObserver()
    }

    fun isUserLoggedIn() = userRepository.isLoggedIn()
    fun signOut() = userRepository.signOut()
    fun getUserEmail() = userRepository.getUserEmail()
}