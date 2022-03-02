package com.arkadiusz.dayscounter.data.repository

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository @Inject constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()

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

    suspend fun signInWithLoginAndPassword(login: String, password: String): Boolean {
        return when {
            login.isEmpty() || password.isEmpty() -> false
            else -> suspendCoroutine { continuation ->
                firebaseAuth.signInWithEmailAndPassword(login, password).addOnCompleteListener {
                    continuation.resume(it.isSuccessful)
                }
            }
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> false
            else -> suspendCoroutine { continuation ->
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    continuation.resume(it.isSuccessful)
                }
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}