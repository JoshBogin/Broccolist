package com.broccolist.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.core.data.GuestListStore
import com.broccolist.core.data.GuestModeRepository
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val supabaseRepository: SupabaseRepository,
    private val guestModeRepository: GuestModeRepository,
    private val guestListStore: GuestListStore,
) : ViewModel() {

    /**
     * Authenticated-user list cache — only `refresh()` mutates it. Guest
     * users read from [GuestListStore.lists] directly via [combine] below.
     */
    private val _authenticatedLists = MutableStateFlow<List<CheckList>>(emptyList())

    /** Display name extracted from the session's email (or "Guest"). */
    private val _displayName = MutableStateFlow("Guest")

    val state = combine(
        guestModeRepository.isGuest,
        guestListStore.lists,
        _authenticatedLists,
        _displayName,
    ) { isGuest, guestLists, authLists, displayName ->
        val lists = if (isGuest) guestLists else authLists
        DashboardState(
            displayName = displayName,
            isGuest = isGuest,
            personalListCount = lists.size,
            // Phase 4 + 5 will fill these in.
            sharedListCount = 0,
            recipeCount = 0,
            recentLists = lists
                .sortedByDescending { it.editedAt ?: "" }
                .take(MAX_RECENT_LISTS),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DashboardState(),
    )

    /**
     * Pull the latest data for the supplied session (or refresh the
     * derived state for a guest). Call on screen entry — the dashboard is
     * a snapshot view; we don't keep a Realtime subscription open here.
     */
    fun refresh(userSession: UserSession?) {
        _displayName.value = userSession?.user?.email?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
            ?: "Guest"
        val userId = userSession?.user?.id
        if (userId.isNullOrBlank()) return
        viewModelScope.launch {
            _authenticatedLists.value = supabaseRepository.getListsForUser(userId)
        }
    }

    private companion object {
        const val MAX_RECENT_LISTS = 3
    }
}

data class DashboardState(
    val displayName: String = "",
    val isGuest: Boolean = false,
    val personalListCount: Int = 0,
    val sharedListCount: Int = 0,
    val recipeCount: Int = 0,
    val recentLists: List<CheckList> = emptyList(),
)
