package com.broccolist.personallists.presentation

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel backing the Personal Lists index. Authenticated users sync
 * through [SupabaseRepository]; guests read/write via [GuestListStore].
 * Phase 3 will collapse the two paths once SQLDelight is the source of
 * truth on both sides.
 */
class PersonalListsViewModel(
    private val supabaseRepository: SupabaseRepository,
    private val guestModeRepository: GuestModeRepository,
    private val guestListStore: GuestListStore,
) : ViewModel() {

    private val _authenticatedLists = MutableStateFlow<List<CheckList>>(emptyList())
    private var cachedUserSession: UserSession? = null

    val state = combine(
        guestModeRepository.isGuest,
        guestListStore.lists,
        _authenticatedLists,
    ) { isGuest, guestLists, authLists ->
        PersonalListsState(
            isGuest = isGuest,
            lists = if (isGuest) guestLists else authLists,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PersonalListsState(),
    )

    fun refresh(userSession: UserSession?) {
        cachedUserSession = userSession
        val userId = userSession?.user?.id ?: return
        viewModelScope.launch {
            _authenticatedLists.value = supabaseRepository.getListsForUser(userId)
        }
    }

    fun createList(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (guestModeRepository.isGuest.value) {
            guestListStore.createList(trimmed)
            return
        }
        val userId = cachedUserSession?.user?.id ?: return
        viewModelScope.launch {
            val created = supabaseRepository.createList(trimmed, userId) ?: return@launch
            _authenticatedLists.update { it + created }
        }
    }

    fun renameList(listId: Int, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        if (guestModeRepository.isGuest.value) {
            guestListStore.renameList(listId, trimmed)
            return
        }
        viewModelScope.launch {
            supabaseRepository.renameList(listId, trimmed)
            _authenticatedLists.update { lists ->
                lists.map { if (it.id == listId) it.copy(name = trimmed) else it }
            }
        }
    }

    fun deleteList(listId: Int) {
        if (guestModeRepository.isGuest.value) {
            guestListStore.deleteList(listId)
            return
        }
        viewModelScope.launch {
            supabaseRepository.deleteList(listId)
            _authenticatedLists.update { lists -> lists.filter { it.id != listId } }
        }
    }
}

data class PersonalListsState(
    val isGuest: Boolean = false,
    val lists: List<CheckList> = emptyList(),
)
