package com.broccolist.app.presentation

import io.github.jan.supabase.auth.user.UserSession

/**
 * Effective authentication state seen by the UI layer.
 *
 * Wraps Supabase's [io.github.jan.supabase.auth.status.SessionStatus] together
 * with the local guest-mode flag so [AuthenticatedContent] only needs one
 * input to decide what to render.
 */
sealed interface AppAuthState {
    data object Initializing : AppAuthState
    data object NotAuthenticated : AppAuthState
    data class Authenticated(val session: UserSession) : AppAuthState

    /**
     * No Supabase session, but the user opted to continue locally. Personal
     * lists and recipes are available; shared lists and friends are gated.
     */
    data object Guest : AppAuthState

    data object RefreshFailure : AppAuthState
}
