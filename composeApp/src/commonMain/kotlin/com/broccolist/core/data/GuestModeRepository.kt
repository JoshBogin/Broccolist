package com.broccolist.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks whether the current session is a guest (no Supabase account).
 *
 * Phase 0 stores the flag in memory only — exiting the app drops it. Phase 3
 * will replace this with a SQLDelight-backed flag plus a `user_id` sentinel
 * on every locally-owned row, and add a migration step that uploads guest
 * data when the user later signs up or signs in.
 *
 * Single-source-of-truth pattern: any consumer that needs to know whether
 * we're in guest mode collects [isGuest]. Mutations go through
 * [enterGuestMode] / [exitGuestMode] so the call sites are easy to find.
 */
interface GuestModeRepository {
    val isGuest: StateFlow<Boolean>
    fun enterGuestMode()
    fun exitGuestMode()
}

internal class InMemoryGuestModeRepository : GuestModeRepository {
    private val _isGuest = MutableStateFlow(false)
    override val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    override fun enterGuestMode() {
        _isGuest.value = true
    }

    override fun exitGuestMode() {
        _isGuest.value = false
    }
}
