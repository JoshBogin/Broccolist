package com.broccolist.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.core.data.AuthApi
import com.broccolist.core.data.GuestModeRepository
import com.broccolist.core.data.SessionLoader
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val authApi: AuthApi,
    private val guestModeRepository: GuestModeRepository,
    private val sessionLoader: SessionLoader,
) : ViewModel() {
    init {
        // Load the saved session (idempotent — on Android the process
        // lifecycle observer also calls this; SessionLoader dedupes).
        sessionLoader.start()

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
        sessionLoader.initialLoadComplete,
    ) { sessionStatus, isGuest, initialLoadComplete ->
        when (sessionStatus) {
            is SessionStatus.Authenticated -> AppAuthState.Authenticated(sessionStatus.session)
            is SessionStatus.RefreshFailure -> AppAuthState.RefreshFailure
            is SessionStatus.Initializing -> AppAuthState.Initializing
            // With autoLoadFromStorage off, supabase-kt reports
            // NotAuthenticated before SessionLoader has even read storage.
            // Hold on the spinner until that first load finishes so a
            // signed-in user isn't bounced to Login mid-refresh.
            is SessionStatus.NotAuthenticated -> when {
                isGuest -> AppAuthState.Guest
                !initialLoadComplete -> AppAuthState.Initializing
                else -> AppAuthState.NotAuthenticated
            }
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
     * Backs the "Try again" action on [SessionRefreshFailureScreen].
     * Re-loads the saved session from storage and refreshes it (cancelling
     * any attempt parked in supabase-kt's 10s network-retry delay). On
     * success `sessionStatus` flips to `Authenticated` and
     * `AuthenticatedContent` swaps screens automatically; on another network
     * failure we stay here; if Supabase rejects the refresh token the session
     * is cleared and the user lands on Login.
     */
    fun retrySession() {
        sessionLoader.retry()
    }

    /**
     * Backs the "Sign in again" action on [SessionRefreshFailureScreen].
     * Signs out (drops the bad refresh token) and clears guest state so
     * the next launch lands cleanly on Login.
     */
    fun signOutAndExit() {
        guestModeRepository.exitGuestMode()
        viewModelScope.launch {
            sessionLoader.cancel()
            runCatching { authApi.signOut() }
                .onFailure { it.printStackTrace() }
        }
    }
}
