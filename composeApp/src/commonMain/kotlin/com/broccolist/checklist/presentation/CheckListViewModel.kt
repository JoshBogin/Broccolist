package com.broccolist.checklist.presentation

import com.broccolist.app.presentation.MPViewModel
import com.broccolist.checklist.data.CheckListRepository
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckListViewModel(
    private val supabaseClient: SupabaseClient,
    private val checkListRepository: CheckListRepository
) : MPViewModel() {
    private val _sections = MutableStateFlow(listOf<Section>())
    val sections = _sections.asStateFlow()

    fun getSections(listId: Int) {
        coroutineScope.launch {
            checkListRepository.getSections(listId).let {
                _sections.value = it
            }
        }
    }

    fun addSection(section: Section) {
        val currentSections = _sections.value.toMutableList()
        currentSections.add(section)
        _sections.value = currentSections

        // Add the section to the database
        coroutineScope.launch {
            checkListRepository.addSection(section)
        }
    }

    fun removeSection(section: Section) {
        val currentSections = _sections.value.toMutableList()
        currentSections.remove(section)
        _sections.value = currentSections

        // Remove the section from the database
        coroutineScope.launch {
            checkListRepository.removeSection(section)
        }
    }

    fun updateSection(section: Section) {
        val currentSections = _sections.value.toMutableList()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            currentSections[index] = section
            _sections.value = currentSections
        }

        // Update the section in the database
        coroutineScope.launch {
            checkListRepository.updateSection(section)
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

        // Add the item to the database
        coroutineScope.launch {
            checkListRepository.addItem(item)
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

        // Remove the item from the database
        coroutineScope.launch {
            checkListRepository.removeItem(item)
        }
    }

    fun signOut() {
        try {
            coroutineScope.launch {
                supabaseClient.auth.signOut()
            }
            println("Signed out")
        } catch (e: Exception) {
            println("Error during sign out: $e")
        }
    }
}
