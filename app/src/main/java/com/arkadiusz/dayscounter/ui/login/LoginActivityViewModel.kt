package com.arkadiusz.dayscounter.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.repository.UserRepository
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun signInWithLoginAndPassword(email: String, password: String) {
        viewModelScope.launch {
            withProgress {
                when (userRepository.signInWithLoginAndPassword(email, password)) {
                    true -> {
                        databaseRepository.addLocalEventsToCloud()
                        _uiState.update { it.copy(isSignedIn = true) }
                    }
                    false -> _uiState.update {
                        it.copy(userMessageId = R.string.login_activity_wrong_credentials)
                    }
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            withProgress {
                when (userRepository.sendPasswordResetEmail(email)) {
                    true -> _uiState.update {
                        it.copy(userMessageId = R.string.login_activity_password_reset_toast_success)
                    }
                    false -> _uiState.update {
                        it.copy(userMessageId = R.string.login_activity_password_reset_toast_fail)
                    }
                }
            }
        }
    }

    fun onSignInFlowFinish(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            databaseRepository.addLocalEventsToCloud()
            _uiState.update { it.copy(isSignedIn = true) }
        }
        if (result.idpResponse?.error?.errorCode == ErrorCodes.NO_NETWORK) {
            _uiState.update {
                it.copy(userMessageId = R.string.login_activity_connection_problem)
            }
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(userMessageId = null)
        }
    }

    private suspend fun withProgress(block: suspend () -> Unit) {
        _uiState.update { it.copy(isInProgress = true) }
        block()
        _uiState.update { it.copy(isInProgress = false) }
    }

    override fun onCleared() {
        super.onCleared()
        databaseRepository.closeDatabase()
    }
}