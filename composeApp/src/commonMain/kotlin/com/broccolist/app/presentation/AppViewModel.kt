package com.broccolist.app.presentation

import com.broccolist.core.MPViewModel
import com.broccolist.core.data.AuthApi
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    authApi: AuthApi,
) : MPViewModel() {
    val sessionStatus = authApi.sessionStatus().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionStatus.NotAuthenticated(false),
    )
}
