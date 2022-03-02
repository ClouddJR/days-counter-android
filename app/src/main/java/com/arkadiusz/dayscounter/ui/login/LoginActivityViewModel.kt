package com.arkadiusz.dayscounter.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    private var userRepository: UserRepository,
    private var databaseRepository: DatabaseRepository
) : ViewModel(), UserRepository.OnEmailResetListener, UserRepository.OnLoggedListener {

    val loginResult = MutableLiveData<Boolean>()
    val emailResetResult = MutableLiveData<Boolean>()

    init {
        userRepository.addOnEmailResetListener(this)
        userRepository.addOnLoggedListener(this)
    }

    override fun onLoggedResult(wasSuccessful: Boolean) {
        loginResult.value = wasSuccessful
    }

    override fun onEmailReset(wasSuccessful: Boolean) {
        emailResetResult.value = wasSuccessful
    }

    fun signInWithLoginAndPassword(email: String, password: String) {
        userRepository.signInWithLoginAndPassword(email, password)
    }

    fun sendPasswordResetEmail(email: String) {
        userRepository.sendPasswordResetEmail(email)
    }

    fun addLocalEventsToCloud() {
        databaseRepository.addLocalEventsToCloud()
    }

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }
}