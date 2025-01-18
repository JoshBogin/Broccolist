package com.broccolist.checklist.presentation

import com.broccolist.app.MPViewModel
import com.broccolist.checklist.domain.Section
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CheckListViewModel(
    private val supabaseClient: SupabaseClient
) : MPViewModel() {
    private val _sections = MutableStateFlow(listOf<Section>())
    val sections = _sections.asStateFlow()

    fun addSection(section: Section) {
        val currentSections = _sections.value.toMutableList()
        currentSections.add(section)
        _sections.value = currentSections
    }

    fun removeSection(section: Section) {
        val currentSections = _sections.value.toMutableList()

        for (sec in _sections.value) {
            if (sec.name == section.name) {
                currentSections.remove(sec)
                break
            }
        }

        _sections.value = currentSections
    }

    suspend fun signOut() {
        try {
            supabaseClient.auth.signOut()
            println("Signed out")
        } catch (e: Exception) {
            println("Error during sign out: $e")
        }
    }
}
