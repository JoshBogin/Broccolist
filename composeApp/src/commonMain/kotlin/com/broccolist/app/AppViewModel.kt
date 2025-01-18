package com.broccolist.app

import androidx.lifecycle.ViewModel
import com.broccolist.core.data.AuthApi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

expect open class MPViewModel() : ViewModel {
    val coroutineScope: CoroutineScope
}

class AppViewModel(
    private val supabaseClient: SupabaseClient,
    private val authApi: AuthApi
) : MPViewModel() {

    val sessionStatus = authApi.sessionStatus().stateIn(coroutineScope, SharingStarted.Eagerly, SessionStatus.NotAuthenticated(false))
//    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
//    val userState = _userState.asStateFlow()

    // region Auth

    fun loginWithGoogle() {
        coroutineScope.launch {
            kotlin.runCatching {
                authApi.signInWithGoogle()
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

//    fun isUserLoggedIn() {
//        coroutineScope.launch {
//            try {
//                _userState.value = UserState.Loading
//                val token = supabaseClient.auth.currentAccessTokenOrNull()
//                if(token.isNullOrEmpty()) {
//                    _userState.value = UserState.Success("User not logged in!")
//                } else {
//                    supabaseClient.auth.retrieveUser(token)
//                    supabaseClient.auth.refreshCurrentSession()
//                    _userState.value = UserState.Success("User already logged in!")
//                }
//            } catch (e: RestException) {
//                _userState.value = UserState.Error(e.error)
//            }
//        }
//    }

    // endregion

}
