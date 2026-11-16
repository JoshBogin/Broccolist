package com.broccolist.app.data

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object ListGraph : Route

    @Serializable
    data object Login : Route

    /** Phase 1 entry point after auth — tile grid + Jump Back In. */
    @Serializable
    data object Dashboard : Route

    /** Index of the user's personal lists (CRUD-able). */
    @Serializable
    data object PersonalLists : Route

    /**
     * Lists the user has access to via sharing. Phase 1 ships an empty-state
     * placeholder; Phase 4 will populate this from a `list_members` join.
     */
    @Serializable
    data object SharedLists : Route

    /**
     * The user's saved recipes. Phase 1 ships an empty-state placeholder;
     * Phase 5 will add recipe CRUD + parsing.
     */
    @Serializable
    data object Recipes : Route

    /**
     * Friends list + invites. Phase 1 ships a stub; Phase 4 adds the
     * friends system (profiles, friend_requests, friendships tables).
     */
    @Serializable
    data object Profile : Route

    /**
     * Single checklist view. Parameterized by `listId` now that Phase 1
     * supports multiple personal lists per user.
     */
    @Serializable
    data class CheckList(val listId: Int) : Route

    /** Step 1 of password reset — user enters email to request a link. */
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
