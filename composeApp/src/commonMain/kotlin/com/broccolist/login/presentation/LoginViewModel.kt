package com.broccolist.login.presentation

import com.broccolist.app.MPViewModel
import com.broccolist.core.data.AuthApi
import com.broccolist.core.domain.UserState
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    val supabaseClient: SupabaseClient,
    private val authApi: AuthApi,
) : MPViewModel() {
    private val _userState = MutableStateFlow(UserState.NotAuthenticated)
    val userState = _userState.asStateFlow()

    fun loginWithGoogle() {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.signInWithGoogle()
            }
        }
    }

    // TODO add Apple login
}
