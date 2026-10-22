package com.broccolist.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.core.data.AuthApi
import com.broccolist.core.data.GuestModeRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApi: AuthApi,
    private val guestModeRepository: GuestModeRepository,
) : ViewModel() {
    val composeAuth = authApi.composeAuth()
    val sessionStatus = authApi.sessionStatus().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionStatus.NotAuthenticated(false),
    )
    val alert = MutableStateFlow<String?>(null)
    val passwordReset = MutableStateFlow<Boolean>(false)

    /**
     * True while sign-up or sign-in is in flight. Used to swap the Login
     * button into a spinner and to debounce double-taps. Without this the
     * user gets no feedback that anything is happening while we wait for
     * the network round-trip (up to ~15s if Supabase is unreachable).
     */
    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating = _isAuthenticating.asStateFlow()

    fun signUp(email: String, password: String) {
        if (_isAuthenticating.value) return
        _isAuthenticating.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.signUp(email, password)
            }.onSuccess {
                // Supabase is configured to require email confirmation, so
                // there will be no session until the user clicks the link.
                alert.value = "Check your email to verify your account, then sign in."
            }.onFailure {
                alert.value = describeAuthError(it, action = "registering")
            }
            _isAuthenticating.value = false
        }
    }

    fun login(email: String, password: String) {
        if (_isAuthenticating.value) return
        _isAuthenticating.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.signIn(email, password)
            }.onFailure { throwable ->
                throwable.printStackTrace()
                alert.value = describeAuthError(throwable, action = "logging in")
            }
            _isAuthenticating.value = false
        }
    }

    fun resendVerificationEmail(email: String) {
        if (email.isBlank()) {
            alert.value = "Enter your email above first, then tap resend."
            return
        }
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.resendVerificationEmail(email)
            }.onSuccess {
                alert.value = "Verification email sent. Check your inbox."
            }.onFailure {
                alert.value = "Couldn't resend verification email: ${it.message}"
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authApi.signInWithGoogle()
        }
    }

    fun continueAsGuest() {
        guestModeRepository.enterGuestMode()
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.resetPassword(email)
            }.onSuccess {
                alert.value = "Password reset link sent. Check your email."
            }.onFailure {
                alert.value = "Couldn't send reset email: ${it.message}"
            }
        }
    }

    fun changePassword(password: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.changePassword(password)
            }.onSuccess {
                alert.value = "Password changed successfully!"
            }.onFailure {
                alert.value = "There was an error while changing the password: ${it.message}"
            }
        }
    }

    fun resetAlert() {
        alert.value = null
    }

    /**
     * Maps a Supabase / Ktor exception to a user-facing string. The exact
     * exception type depends on the platform (Android: UnknownHostException
     * / ConnectException; iOS: NSURLError), so we match by message keyword
     * — Ktor's HttpTimeout plugin also throws messages containing "timeout".
     * The unconfirmed-email branch stays separate because Supabase returns
     * a deterministic message for it.
     */
    private fun describeAuthError(throwable: Throwable, action: String): String {
        val message = (throwable.message ?: throwable::class.simpleName ?: "").lowercase()
        return when {
            "email not confirmed" in message || "not_confirmed" in message ->
                "Your email isn't verified yet. Check your inbox for the verification link."
            "unable to resolve host" in message ||
                "unknownhost" in message ||
                "failed to connect" in message ||
                "no address associated" in message ||
                "network is unreachable" in message ||
                "no internet" in message ||
                "timeout" in message ||
                "connectexception" in message ||
                "sockettimeout" in message ||
                "could not connect" in message ->
                "Couldn't connect to Broccolist. Check your internet and try again."
            "invalid login credentials" in message || "invalid_grant" in message ->
                "That email and password don't match. Try again."
            else ->
                "There was an error while $action. Please try again."
        }
    }
}
