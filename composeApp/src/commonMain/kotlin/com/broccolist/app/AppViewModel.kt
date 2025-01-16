package com.broccolist.app

import androidx.lifecycle.ViewModel
import com.broccolist.core.data.network.AuthApi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

expect open class MPViewModel() : ViewModel {
    val coroutineScope: CoroutineScope
}
class AppViewModel(
    val supabaseClient: SupabaseClient,
    private val authApi: AuthApi
) : MPViewModel() {

    val sessionStatus = authApi.sessionStatus().stateIn(coroutineScope, SharingStarted.Eagerly, SessionStatus.NotAuthenticated(false))
    val alert = MutableStateFlow<String?>(null)
    val passwordReset = MutableStateFlow<Boolean>(false)

    //Auth

    fun signUp(email: String, password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.signUp(email, password)
            }.onSuccess {
                alert.value = "Successfully registered! Check your E-Mail to verify your account."
            }.onFailure {
                alert.value = "There was an error while registering: ${it.message}"
            }
        }
    }

    fun login(email: String, password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.signIn(email, password)
            }.onFailure {
                it.printStackTrace()
                alert.value = "There was an error while logging in. Check your credentials and try again."
            }
        }
    }

    fun loginWithGoogle() {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.signInWithGoogle()
            }
        }
    }

    fun loginWithOTP(email: String, code: String, reset: Boolean) {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.verifyOtp(email, code)
            }.onSuccess {
                passwordReset.value = reset
            }.onFailure {
                alert.value = "There was an error while verifying the OTP: ${it.message}"
            }
        }
    }

    fun resetPassword(email: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.resetPassword(email)
            }
        }
    }

    fun changePassword(password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.changePassword(password)
            }.onSuccess {
                alert.value = "Password changed successfully!"
            }.onFailure {
                alert.value = "There was an error while changing the password: ${it.message}"
            }
        }
    }

    fun logout() {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.signOut()
            }
        }
    }

    fun disconnectFromRealtime() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.realtime.removeAllChannels()
            }
        }
    }

}
