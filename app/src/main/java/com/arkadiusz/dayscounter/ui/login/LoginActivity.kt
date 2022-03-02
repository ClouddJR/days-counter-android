package com.arkadiusz.dayscounter.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.main.MainActivity
import com.arkadiusz.dayscounter.util.ThemeUtils.getThemeFromPreferences
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.*

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginActivityViewModel by viewModels()

    private val allProviders = listOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build()
    )
    private val googleProvider = listOf(AuthUI.IdpConfig.GoogleBuilder().build())

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startActivity<MainActivity>()
                finish()
                viewModel.addLocalEventsToCloud()
            } else {
                if (result.idpResponse?.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    longToast(getString(R.string.login_activity_connection_problem))
                }
            }
            progressBar.visibility = View.GONE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupActionBar()
        listenToLoginEvents()
        initLogInButtons()
        initGoogleLoginButtonText()
    }

    override fun onBackPressed() {
        startActivity<MainActivity>()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity<MainActivity>()
        finish()
        return true
    }

    private fun setupActionBar() {
        supportActionBar?.title = getString(R.string.login_activity_login_button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun listenToLoginEvents() {
        viewModel.loginResult.observe(this) { wasSuccessful ->
            if (wasSuccessful) {
                viewModel.addLocalEventsToCloud()
                startActivity<MainActivity>()
                finish()
            } else {
                longToast(getString(R.string.login_activity_wrong_credentials))
                progressBar.visibility = View.GONE
            }
        }

        viewModel.emailResetResult.observe(this) { wasSuccessful ->
            val toastText = when (wasSuccessful) {
                true -> getString(R.string.login_activity_password_reset_toast_success)
                else -> getString(R.string.login_activity_password_reset_toast_fail)
            }
            longToast(toastText)
        }
    }

    private fun initLogInButtons() {
        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            viewModel.signInWithLoginAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        signInWithGoogleButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            signInLauncher.launch(buildAuthUi(googleProvider))
        }

        createAccountButton.setOnClickListener {
            signInLauncher.launch(buildAuthUi(allProviders))
        }

        forgotPasswordButton.setOnClickListener {
            displayResetPasswordDialog()
        }
    }

    private fun buildAuthUi(type: List<AuthUI.IdpConfig>): Intent {
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
                viewModel.sendPasswordResetEmail(editText.text.toString().trim())
            }

            negativeButton(getString(R.string.add_activity_back_button_cancel)) {
                it.dismiss()
            }
            customView {
                verticalLayout {
                    padding = dip(16)

                    textView {
                        textSize = 20f
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

    private fun initGoogleLoginButtonText() {
        (signInWithGoogleButton.getChildAt(0) as TextView).text =
            getString(R.string.login_activity_google)
    }
}