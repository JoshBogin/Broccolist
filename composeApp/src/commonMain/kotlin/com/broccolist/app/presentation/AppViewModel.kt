package com.broccolist.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.core.data.AuthApi
import com.broccolist.core.data.GuestModeRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val authApi: AuthApi,
    private val guestModeRepository: GuestModeRepository,
) : ViewModel() {
    init {
        // If a guest later signs in (email/password, Google, or via email
        // verification deep link), drop the guest flag so downstream
        // ViewModels stop short-circuiting Supabase calls. Without this,
        // CheckListViewModel would still treat the user as a guest and
        // write only to in-memory state. Phase 3 will replace this with an
        // explicit migration step that uploads the in-memory data first.
        viewModelScope.launch {
            authApi.sessionStatus().collect { status ->
                if (status is SessionStatus.Authenticated &&
                    guestModeRepository.isGuest.value
                ) {
                    guestModeRepository.exitGuestMode()
                }
            }
        }
    }

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

    /**
     * Backs the "Try again" action on [SessionRefreshFailureScreen]. If the
     * refresh succeeds, `sessionStatus` flips back to `Authenticated` and
     * `AuthenticatedContent` swaps screens automatically. If it throws,
     * we stay on the failure screen and the user can fall back to
     * "Sign in again".
     */
    fun retrySession() {
        viewModelScope.launch {
            runCatching { authApi.refreshSession() }
                .onFailure { it.printStackTrace() }
        }
    }

    /**
     * Backs the "Sign in again" action on [SessionRefreshFailureScreen].
     * Signs out (drops the bad refresh token) and clears guest state so
     * the next launch lands cleanly on Login.
     */
    fun signOutAndExit() {
        guestModeRepository.exitGuestMode()
        viewModelScope.launch {
            runCatching { authApi.signOut() }
                .onFailure { it.printStackTrace() }
        }
    }
}
