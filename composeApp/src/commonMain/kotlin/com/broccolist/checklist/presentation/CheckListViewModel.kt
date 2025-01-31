package com.broccolist.checklist.presentation

import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import com.broccolist.core.MPViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        upsertLocalSection(section)

        // Add the section to the database
        coroutineScope.launch {
            val newSection = supabaseRepository.addSection(section)
            upsertLocalSection(newSection)
        }
    }

    private fun upsertLocalSection(section: Section) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        // remove the section if it already exists before id is set
        currentSections.find {
            it.checkEqualityWithNullId(section)
        }?.let {
            currentSections.remove(it) // TODO might have to do some logic to like combine the items of the
            // two sections to make sure you don't delete the wrong one
        }
        currentSections.add(section)
        _checkLists.update {
            it?.copy(
                sections = currentSections
            )
        }
    }

    fun removeSection(section: Section) {
        if (section.id == null) return

        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        currentSections.remove(section)
        _checkLists.update {
            it?.copy(
                sections = currentSections
            )
        }

        // Remove the section from the database
        coroutineScope.launch {
            supabaseRepository.removeSection(section)
        }
    }

    fun updateSection(section: Section) {
        if (section.id == null) return

        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            currentSections[index] = section
            _checkLists.update {
                it?.copy(
                    sections = currentSections
                )
            }
        }

        // Update the section in the database
        coroutineScope.launch {
            supabaseRepository.updateSection(section)
        }
    }

    // This function is used to add an item to a section. If the section does not exist, it will be created.
    fun addItemToSection(section: Section, item: SectionItem) {
        upsertLocalItem(section, item)

        // Add the item to the database
        coroutineScope.launch {
            val newItem = supabaseRepository.addItem(item)
            upsertLocalItem(section, newItem)
        }

    }

    private fun upsertLocalItem(
        section: Section,
        item: SectionItem
    ) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            // remove the item if it already exists before id is set
            currentItems.find {
                it.checkEqualityWithNullId(item)
            }?.let {
                currentItems.remove(it)
            }
            currentItems.add(item)
            currentSections[index] = currentSections[index].copy(items = currentItems)
            _checkLists.update {
                it?.copy(
                    sections = currentSections
                )
            }
        } else {
            val newSection = section.copy(items = listOf(item))
            currentSections.add(newSection)
            _checkLists.update {
                it?.copy(
                    sections = currentSections
                )
            }
        }
    }

    fun removeItemFromSection(section: Section, item: SectionItem) {
        if (section.id == null || item.id == null) {
            return
        }

        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }
        if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            currentItems.remove(item)
            currentSections[index] = currentSections[index].copy(items = currentItems)
            _checkLists.update {
                it?.copy(
                    sections = currentSections
                )
            }
        }

        // Remove the item from the database
        coroutineScope.launch {
            supabaseRepository.removeItem(item)
        }
    }

    fun updateItem(item: SectionItem) {
        if (item.id == null) return

        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val sectionIndex = currentSections.indexOfFirst { it.id == item.sectionId }
        if (sectionIndex != -1) {
            val currentItems = currentSections[sectionIndex].items?.toMutableList() ?: mutableListOf()
            val itemIndex = currentItems.indexOfFirst { it.id == item.id }
            if (itemIndex != -1) {
                currentItems[itemIndex] = item
                currentSections[sectionIndex] = currentSections[sectionIndex].copy(items = currentItems)
                _checkLists.update {
                    it?.copy(
                        sections = currentSections
                    )
                }
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
