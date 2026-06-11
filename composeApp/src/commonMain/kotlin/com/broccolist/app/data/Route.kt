package com.broccolist.app.data

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object ListGraph: Route

    @Serializable
    data object Login: Route

    @Serializable
    data object CheckList: Route

    /** Step 1 — user enters email to request a reset link. */
    @Serializable
    data object ResetPasswordRequest : Route

    /**
     * Step 2 — "Check your email" confirmation. The user taps the link in
     * their inbox (which silently creates a recovery session via the deep
     * link handler), then taps "I've got the link — continue" to advance.
     */
    @Serializable
    data class ResetPasswordSent(val email: String) : Route

    /** Step 3 — user enters a new password (requires recovery session). */
    @Serializable
    data object ResetPasswordCreate : Route

    /** Step 4 — success confirmation; "Back to login" returns to Login. */
    @Serializable
    data object ResetPasswordSuccess : Route
}
