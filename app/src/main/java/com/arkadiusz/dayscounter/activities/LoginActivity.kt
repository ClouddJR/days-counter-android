package com.arkadiusz.dayscounter.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.repositories.UserRepository
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

/**
 * Created by Arkadiusz on 28.02.2018
 */

class LoginActivity : AppCompatActivity(), UserRepository.OnLoggedListener, UserRepository.OnEmailResetListener {

    private val RC_SIGN_IN = 123
    private val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
            AuthUI.IdpConfig.GoogleBuilder().build())
    private val googleProvider = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initUserRepository()
        initGoogleLoginButtonText()
        initLogInButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                if (response?.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.login_activity_connection_problem, Toast.LENGTH_LONG).show()
                }
            }
        }

        progressBar.visibility = View.GONE
    }

    private fun initUserRepository() {
        userRepository.addOnLoggedListener(this)
        userRepository.addOnEmailResetListener(this)
    }


    private fun initGoogleLoginButtonText() {
        (signInWithGoogleButton.getChildAt(0) as TextView).text = getString(R.string.login_activity_google)
    }

    private fun initLogInButtons() {
        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            userRepository.signInWithLoginAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        signInWithGoogleButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            startActivityForResult(buildAuthUi(googleProvider), RC_SIGN_IN)
        }

        createAccountButton.setOnClickListener {
            startActivityForResult(buildAuthUi(providers), RC_SIGN_IN)
        }

        forgotPasswordButton.setOnClickListener {
            displayResetPasswordDialog()
        }
    }

    private fun buildAuthUi(type: ArrayList<AuthUI.IdpConfig>): Intent {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(type)
                .setTheme(R.style.AppTheme)
                .build()
    }

    override fun onLoggedResult(wasSuccessful: Boolean) {
        if (wasSuccessful) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, R.string.login_activity_wrong_credentials, Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
        }
    }

    override fun onEmailReset(wasSuccessful: Boolean) {
        val toastText = when (wasSuccessful) {
            true -> getString(R.string.login_activity_password_reset_toast_success)
            else -> getString(R.string.login_activity_password_reset_toast_fail)
        }
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show()
    }


    private fun displayResetPasswordDialog() {

        val dialog = AlertDialog.Builder(LoginActivity@ this, R.style.ResetDialog)
        val editText = EditText(this)
        editText.hint = getString(R.string.login_activity_password_reset_form_edit_text)
        val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        f.isAccessible = true
        f.set(editText, R.color.colorAccent)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        editText.layoutParams = layoutParams
        dialog.setView(editText)
        dialog.setPositiveButton(getString(R.string.login_activity_password_reset_form_button), { _, _ ->
            userRepository.sendPasswordResetEmail(editText.text.toString().trim())
        })
        dialog.show()
    }
}
