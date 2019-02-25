package com.arkadiusz.dayscounter.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.repositories.DatabaseProvider
import com.arkadiusz.dayscounter.repositories.UserRepository
import com.arkadiusz.dayscounter.utils.ThemeUtils.getThemeFromPreferences
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.*
import java.util.*

class LoginActivity : AppCompatActivity(), UserRepository.OnEmailResetListener,
        UserRepository.OnLoggedListener {

    private val userRepository = UserRepository()

    private val RC_SIGN_IN = 123
    private val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build())
    private val googleProvider = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initUserRepository()
        initLogInButtons()
        initGoogleLoginButtonText()
    }

    override fun onBackPressed() {
        startActivity<MainActivity>()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                startActivity<MainActivity>()
                finish()
                DatabaseProvider.provideRepository().addLocalEventsToCloud()
            } else {
                if (response?.errorCode == ErrorCodes.NO_NETWORK) {
                    longToast(getString(R.string.login_activity_connection_problem))
                }
            }
        }

        progressBar.visibility = View.GONE
    }

    private fun initUserRepository() {
        userRepository.addOnLoggedListener(this)
        userRepository.addOnEmailResetListener(this)
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
                .setTheme(getThemeFromPreferences(false, this))
                .build()
    }

    private fun displayResetPasswordDialog() {
        lateinit var editText: EditText
        alert {
            positiveButton(getString(R.string.login_activity_password_reset_form_button)) {
                userRepository.sendPasswordResetEmail(editText.text.toString().trim())
            }

            negativeButton(getString(R.string.add_activity_back_button_cancel)) {
                it.dismiss()
            }
            customView {
                verticalLayout {
                    padding = dip(16)

                    textView {
                        textSize = 20f
                        textColor = ContextCompat.getColor(this@LoginActivity, R.color.black)
                        text = getString(R.string.login_activity_password_reset_dialog)
                    }.lparams {
                        bottomMargin = 32
                    }
                    editText = editText {
                        hint = getString(R.string.login_activity_password_reset_form_edit_text)
                    }
                }
            }
        }.show()
    }

    override fun onLoggedResult(wasSuccessful: Boolean) {
        if (wasSuccessful) {
            DatabaseProvider.provideRepository().addLocalEventsToCloud()
            startActivity<MainActivity>()
            finish()
        } else {
            longToast(getString(R.string.login_activity_wrong_credentials))
            progressBar.visibility = View.GONE
        }
    }

    private fun initGoogleLoginButtonText() {
        (signInWithGoogleButton.getChildAt(0) as TextView).text = getString(R.string.login_activity_google)
    }

    override fun onEmailReset(wasSuccessful: Boolean) {
        val toastText = when (wasSuccessful) {
            true -> getString(R.string.login_activity_password_reset_toast_success)
            else -> getString(R.string.login_activity_password_reset_toast_fail)
        }
        longToast(toastText)
    }
}