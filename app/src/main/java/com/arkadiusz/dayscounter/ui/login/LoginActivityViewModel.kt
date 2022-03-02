package com.arkadiusz.dayscounter.ui.login

import androidx.lifecycle.LiveData
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

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _emailResetResult = MutableLiveData<Boolean>()
    val emailResetResult: LiveData<Boolean> = _emailResetResult

    fun signInWithLoginAndPassword(email: String, password: String) {
        viewModelScope.launch {
            val wasSuccessful = userRepository.signInWithLoginAndPassword(email, password)
            _loginResult.value = wasSuccessful
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val wasSuccessful = userRepository.sendPasswordResetEmail(email)
            _emailResetResult.value = wasSuccessful
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