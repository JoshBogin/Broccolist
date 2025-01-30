package com.broccolist.checklist.presentation

import com.broccolist.app.presentation.MPViewModel
import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckListViewModel(
    private val supabaseClient: SupabaseClient,
    private val supabaseRepository: SupabaseRepository
) : MPViewModel() {
    private val _checkLists = MutableStateFlow<CheckList?>(null)
    val checkLists = _checkLists.asStateFlow()

    fun getList(userSession: UserSession) {
        coroutineScope.launch {
            val userId = userSession.user?.id ?: ""
            _checkLists.value = supabaseRepository.getCheckList(userId)
        }
    }

    fun addSection(section: Section) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        currentSections.add(section)
        _checkLists.value?.sections = currentSections

        // Add the section to the database
        coroutineScope.launch {
            supabaseRepository.addSection(section)
        }
    }

    fun removeSection(section: Section) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        currentSections.remove(section)
        _checkLists.value?.sections = currentSections

        // Remove the section from the database
        coroutineScope.launch {
            supabaseRepository.removeSection(section)
        }
    }

    fun updateSection(section: Section) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            currentSections[index] = section
            _checkLists.value?.sections = currentSections
        }

        // Update the section in the database
        coroutineScope.launch {
            supabaseRepository.updateSection(section)
        }
    }

    // This function is used to add an item to a section. If the section does not exist, it will be created.
    fun addItemToSection(section: Section, item: SectionItem) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            currentItems.add(item)
            currentSections[index] = currentSections[index].copy(items = currentItems)
            _checkLists.value?.sections = currentSections
        } else {
            val newSection = section.copy(items = listOf(item))
            currentSections.add(newSection)
            _checkLists.value?.sections = currentSections
        }

        // Add the item to the database
        coroutineScope.launch {
            supabaseRepository.addItem(item)
        }
    }

    fun removeItemFromSection(section: Section, item: SectionItem) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            currentItems.remove(item)
            currentSections[index] = currentSections[index].copy(items = currentItems)
            _checkLists.value?.sections = currentSections
        }

        // Remove the item from the database
        coroutineScope.launch {
            supabaseRepository.removeItem(item)
        }
    }

    fun updateItem(item: SectionItem) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val sectionIndex = currentSections.indexOfFirst { it.id == item.sectionId }
        if (sectionIndex != -1) {
            val currentItems = currentSections[sectionIndex].items?.toMutableList() ?: mutableListOf()
            val itemIndex = currentItems.indexOfFirst { it.id == item.id }
            if (itemIndex != -1) {
                currentItems[itemIndex] = item
                currentSections[sectionIndex] = currentSections[sectionIndex].copy(items = currentItems)
                _checkLists.value?.sections = currentSections
            }
        }

        // Update the item in the database
        coroutineScope.launch {
            supabaseRepository.updateItem(item)
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
