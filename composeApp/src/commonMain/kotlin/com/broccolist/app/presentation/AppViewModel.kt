package com.broccolist.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.core.data.AuthApi
import com.broccolist.core.data.GuestModeRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    authApi: AuthApi,
    private val guestModeRepository: GuestModeRepository,
) : ViewModel() {
    /**
     * Combined auth state: Supabase session takes precedence (a real session
     * always wins), then guest mode, then the various unauthenticated /
     * initializing states from Supabase.
     */
    val authState = combine(
        authApi.sessionStatus(),
        guestModeRepository.isGuest,
    ) { sessionStatus, isGuest ->
        when (sessionStatus) {
            is SessionStatus.Authenticated -> AppAuthState.Authenticated(sessionStatus.session)
            is SessionStatus.RefreshFailure -> AppAuthState.RefreshFailure
            is SessionStatus.Initializing -> AppAuthState.Initializing
            is SessionStatus.NotAuthenticated -> if (isGuest) AppAuthState.Guest else AppAuthState.NotAuthenticated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppAuthState.Initializing,
    )

    /** Called from the sign-out path to drop guest state alongside Supabase. */
    fun exitGuestMode() {
        guestModeRepository.exitGuestMode()
    }
}
