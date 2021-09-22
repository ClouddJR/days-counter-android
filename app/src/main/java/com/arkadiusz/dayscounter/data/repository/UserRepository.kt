package com.arkadiusz.dayscounter.data.repository

import com.google.firebase.auth.FirebaseAuth

class UserRepository {

    private lateinit var loggedListener: OnLoggedListener
    private lateinit var emailResetListener: OnEmailResetListener

    private val firebaseAuth = FirebaseAuth.getInstance()

    interface OnLoggedListener {
        fun onLoggedResult(wasSuccessful: Boolean)
    }

    interface OnEmailResetListener {
        fun onEmailReset(wasSuccessful: Boolean)
    }

    fun getUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    fun isLoggedIn(): Boolean {
        val currentUser = firebaseAuth.currentUser
        return currentUser != null
    }

    fun getUserEmail(): String {
        return firebaseAuth.currentUser?.email ?: ""
    }

    fun signInWithLoginAndPassword(login: String, password: String) {
        if (login.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(login, password).addOnCompleteListener {
                loggedListener.onLoggedResult(it.isSuccessful)
            }
        } else {
            loggedListener.onLoggedResult(false)
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isNotEmpty()) {
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                emailResetListener.onEmailReset(it.isSuccessful)
            }
        } else {
            emailResetListener.onEmailReset(false)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun addOnLoggedListener(listener: OnLoggedListener) {
        loggedListener = listener
    }

    fun addOnEmailResetListener(listener: OnEmailResetListener) {
        emailResetListener = listener
    }
}