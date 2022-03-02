package com.arkadiusz.dayscounter.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    private var userRepository: UserRepository,
    private var databaseRepository: DatabaseRepository,
) : ViewModel() {

    val loginResult = MutableLiveData<Boolean>()
    val emailResetResult = MutableLiveData<Boolean>()

    fun signInWithLoginAndPassword(email: String, password: String) {
        viewModelScope.launch {
            val wasSuccessful = userRepository.signInWithLoginAndPassword(email, password)
            loginResult.value = wasSuccessful
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val wasSuccessful = userRepository.sendPasswordResetEmail(email)
            emailResetResult.value = wasSuccessful
        }
    }

    fun addLocalEventsToCloud() {
        databaseRepository.addLocalEventsToCloud()
    }

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }
}