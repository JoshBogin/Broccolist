package com.broccolist.app

import androidx.lifecycle.ViewModel
import com.broccolist.core.data.AuthApi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

expect open class MPViewModel() : ViewModel {
    val coroutineScope: CoroutineScope
}

class AppViewModel(
    val supabaseClient: SupabaseClient,
    authApi: AuthApi
) : MPViewModel() {

    val sessionStatus = authApi.sessionStatus().stateIn(coroutineScope, SharingStarted.Eagerly, SessionStatus.NotAuthenticated(false))

}
