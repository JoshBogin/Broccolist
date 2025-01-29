package com.broccolist.app.presentation

import androidx.lifecycle.ViewModel
import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.core.data.AuthApi
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

expect open class MPViewModel() : ViewModel {
    val coroutineScope: CoroutineScope
}

class AppViewModel(
    authApi: AuthApi,
    private val supabaseRepository: SupabaseRepository,
) : MPViewModel() {
    val sessionStatus = authApi.sessionStatus().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionStatus.NotAuthenticated(false),
    )

    private val _checkLists = MutableStateFlow<List<CheckList>>(emptyList())
    val checkLists = _checkLists.asStateFlow()

    fun getLists() {
        coroutineScope.launch {
            _checkLists.value = supabaseRepository.getCheckLists()
        }
    }

    fun createList(userSession: UserSession, listId: Int) {
        // Create a new list
        coroutineScope.launch {
            supabaseRepository.createList(CheckList(
                id = listId,
                userId = userSession.user?.id ?: "",
                name = "New List",
                createdAt = Clock.System.now().toString(),
                editedAt = Clock.System.now().toString(),
                sections = emptyList(),
            ))
        }
    }
}
