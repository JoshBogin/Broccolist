package com.broccolist.checklist.domain

sealed class ListLoadingState {
    data object Empty : ListLoadingState()
    data object Loading : ListLoadingState()
    data object Error : ListLoadingState()
    data class Loaded(
        val checkLists: List<CheckList>
    ) : ListLoadingState()
}
