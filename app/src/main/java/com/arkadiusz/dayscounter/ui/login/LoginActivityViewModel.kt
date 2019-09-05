package com.arkadiusz.dayscounter.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.local.DatabaseRepository
import com.arkadiusz.dayscounter.data.local.UserRepository

class LoginActivityViewModel(
        private var userRepository: UserRepository = UserRepository(),
        private var databaseRepository: DatabaseRepository = DatabaseRepository()
) : ViewModel(), UserRepository.OnEmailResetListener,  UserRepository.OnLoggedListener {

    val loginResult = MutableLiveData<Boolean>()
    val emailResetResult = MutableLiveData<Boolean>()

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }

    override fun onLoggedResult(wasSuccessful: Boolean) {
        loginResult.value = wasSuccessful
    }

    override fun onEmailReset(wasSuccessful: Boolean) {
        emailResetResult.value = wasSuccessful
    }

    fun init() {
        userRepository.addOnEmailResetListener(this)
        userRepository.addOnLoggedListener(this)
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
}