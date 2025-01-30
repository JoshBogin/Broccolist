package com.broccolist.app.presentation

import androidx.lifecycle.ViewModel
import com.broccolist.core.data.AuthApi
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

expect open class MPViewModel() : ViewModel {
    val coroutineScope: CoroutineScope
}

class AppViewModel(
    authApi: AuthApi,
) : MPViewModel() {
    val sessionStatus = authApi.sessionStatus().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionStatus.NotAuthenticated(false),
    )
}
