package com.broccolist.checklist.presentation

import com.broccolist.app.presentation.MPViewModel
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
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
        currentSections.remove(section)
        _sections.value = currentSections
    }

    fun updateSection(section: Section) {
        val currentSections = _sections.value.toMutableList()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            currentSections[index] = section
            _sections.value = currentSections
        }
    }

    // This function is used to add an item to a section. If the section does not exist, it will be created.
    fun addItemToSection(section: Section, item: SectionItem) {
        val currentSections = _sections.value.toMutableList()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            currentItems.add(item)
            currentSections[index] = currentSections[index].copy(items = currentItems)
            _sections.value = currentSections
        } else {
            val newSection = section.copy(items = listOf(item))
            currentSections.add(newSection)
            _sections.value = currentSections
        }
    }

    fun removeItemFromSection(section: Section, item: SectionItem) {
        val currentSections = _sections.value.toMutableList()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            currentItems.remove(item)
            currentSections[index] = currentSections[index].copy(items = currentItems)
            _sections.value = currentSections
        }
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
