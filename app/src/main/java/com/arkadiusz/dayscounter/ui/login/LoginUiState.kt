package com.arkadiusz.dayscounter.ui.login

data class LoginUiState(
    val isInProgress: Boolean = false,
    val isSignedIn: Boolean = false,
    val userMessage: String? = null
)