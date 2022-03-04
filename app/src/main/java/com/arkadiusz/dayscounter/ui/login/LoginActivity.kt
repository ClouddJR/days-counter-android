package com.arkadiusz.dayscounter.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.main.MainActivity
import com.arkadiusz.dayscounter.util.ThemeUtils.getThemeFromPreferences
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.*

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginActivityViewModel by viewModels()

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            viewModel.onSignInFlowFinish(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupActionBar()
        observeState()
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

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                var showMessageJob: Job? = null

                viewModel.uiState.collect { state ->
                    if (state.isSignedIn) {
                        startActivity<MainActivity>()
                        finish()
                    }
                    state.userMessage?.let { message ->
                        showMessageJob?.cancel()
                        showMessageJob = launch {
                            longToast(message)
                            delay(3500)
                            viewModel.onMessageShown()
                        }
                    }
                    progressBar.isVisible = state.isInProgress
                }
            }
        }
    }

    private fun initLogInButtons() {
        loginButton.setOnClickListener {
            viewModel.signInWithLoginAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        signInWithGoogleButton.setOnClickListener {
            signInLauncher.launch(buildAuthUi(listOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )))
        }

        createAccountButton.setOnClickListener {
            signInLauncher.launch(buildAuthUi(listOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build()
            )))
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
                viewModel.resetPassword(editText.text.toString().trim())
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