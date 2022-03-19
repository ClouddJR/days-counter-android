package com.arkadiusz.dayscounter.ui.login

import android.app.Activity
import com.arkadiusz.dayscounter.MainCoroutineRule
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.repository.UserRepository
import com.firebase.ui.auth.ErrorCodes.NO_NETWORK
import com.firebase.ui.auth.FirebaseUiException
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginActivityViewModelTest {

    @get:Rule
    val mainCoroutineRule: MainCoroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var databaseRepository: DatabaseRepository

    @InjectMockKs
    private lateinit var viewModel: LoginActivityViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should have correct initial state`() = runTest {
        // given
        val state = viewModel.uiState.value

        // then
        assertFalse(state.isInProgress)
        assertFalse(state.isSignedIn)
        assertNull(state.userMessageId)
    }

    @Test
    fun `should have correct state after successful login`() = runTest {
        // given
        coJustRun { databaseRepository.addLocalEventsToCloud() }
        coEvery { userRepository.signInWithLoginAndPassword(email, password) } returns true

        // when
        viewModel.signInWithLoginAndPassword(email, password)

        // then
        assertTrue(viewModel.uiState.value.isSignedIn)
        verify(exactly = 1) { databaseRepository.addLocalEventsToCloud() }
    }

    @Test
    fun `should have correct state after failed login`() = runTest {
        // given
        coEvery { userRepository.signInWithLoginAndPassword(email, password) } returns false

        // when
        viewModel.signInWithLoginAndPassword(email, password)

        // then
        assertFalse(viewModel.uiState.value.isSignedIn)
        assertEquals(R.string.login_activity_wrong_credentials,
            viewModel.uiState.value.userMessageId)
    }

    @Test
    fun `should have correct state after resetting the password successfully`() = runTest {
        // given
        coEvery { userRepository.sendPasswordResetEmail(email) } returns true

        // when
        viewModel.resetPassword(email)

        // then
        assertEquals(R.string.login_activity_password_reset_toast_success,
            viewModel.uiState.value.userMessageId)
    }

    @Test
    fun `should have correct state after resetting the password failed`() = runTest {
        // given
        coEvery { userRepository.sendPasswordResetEmail(email) } returns false

        // when
        viewModel.resetPassword(email)

        // then
        assertEquals(R.string.login_activity_password_reset_toast_fail,
            viewModel.uiState.value.userMessageId)
    }

    @Test
    fun `should have correct state after sign in flow finished successfully`() = runTest {
        // given
        val result = FirebaseAuthUIAuthenticationResult(Activity.RESULT_OK, null)
        coJustRun { databaseRepository.addLocalEventsToCloud() }

        // when
        viewModel.onSignInFlowFinish(result)

        // then
        assertTrue(viewModel.uiState.value.isSignedIn)
        verify(exactly = 1) { databaseRepository.addLocalEventsToCloud() }
    }

    @Test
    fun `should have correct state after sign in flow failed`() = runTest {
        // given
        val result = FirebaseAuthUIAuthenticationResult(Activity.RESULT_FIRST_USER,
            IdpResponse.from(FirebaseUiException(NO_NETWORK)))
        coJustRun { databaseRepository.addLocalEventsToCloud() }

        // when
        viewModel.onSignInFlowFinish(result)

        // then
        assertFalse(viewModel.uiState.value.isSignedIn)
        assertEquals(R.string.login_activity_connection_problem,
            viewModel.uiState.value.userMessageId)
    }

    @Test
    fun `should update the state when the message has been shown`() = runTest {
        // given
        coEvery { userRepository.sendPasswordResetEmail(email) } returns true

        // when
        viewModel.resetPassword(email)

        assertNotNull(viewModel.uiState.value.userMessageId)

        viewModel.onMessageShown()

        // then
        assertNull(viewModel.uiState.value.userMessageId)
    }

    private companion object {
        const val email = "test@gmail.com"
        const val password = "password"
    }
}