package com.arkadiusz.dayscounter.repositories

import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Arkadiusz on 28.02.2018
 */

class UserRepository {

    private var loggedListener: OnLoggedListener? = null
    private var emailResetListener: OnEmailResetListener? = null
    private val mAuth = FirebaseAuth.getInstance()

    interface OnLoggedListener {
        fun onLoggedResult(wasSuccessful: Boolean)
    }

    interface OnEmailResetListener {
        fun onEmailReset(wasSuccessful: Boolean)
    }

    fun isLoggedIn(): Boolean {
        val currentUser = mAuth.currentUser
        return currentUser != null
    }

    fun signInWithLoginAndPassword(login: String, password: String) {
        if (login.isNotEmpty() && password.isNotEmpty()) {
            mAuth.signInWithEmailAndPassword(login, password).addOnCompleteListener {
                loggedListener?.onLoggedResult(it.isSuccessful)
            }
        } else {
            loggedListener?.onLoggedResult(false)
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (!email.isEmpty()) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                emailResetListener?.onEmailReset(it.isSuccessful)
            }
        } else {
            emailResetListener?.onEmailReset(false)
        }
    }

    fun addOnLoggedListener(listener: OnLoggedListener) {
        loggedListener = listener
    }

    fun addOnEmailResetListener(listener: OnEmailResetListener) {
        emailResetListener = listener
    }

    fun signOut() {
        mAuth.signOut()
    }
}