package com.broccolist.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.core.data.AuthApi
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApi: AuthApi,
) : ViewModel() {
    val composeAuth = authApi.composeAuth()
    val sessionStatus = authApi.sessionStatus().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionStatus.NotAuthenticated(false),
    )
    val alert = MutableStateFlow<String?>(null)
    val passwordReset = MutableStateFlow<Boolean>(false)

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.signUp(email, password)
            }.onFailure {
                alert.value = "There was an error while registering: ${it.message}"
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.signIn(email, password)
            }.onFailure {
                it.printStackTrace()
                alert.value = "There was an error while logging in. Check your credentials and try again."
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authApi.signInWithGoogle()
        }
    }

    fun signInWithApple() {
        viewModelScope.launch {
            authApi.signInWithApple()
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                authApi.resetPassword(email)
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
}
