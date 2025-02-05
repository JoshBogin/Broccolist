package com.broccolist.checklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import com.broccolist.checklist.domain.has
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
) : ViewModel() {
    private val _checkLists = MutableStateFlow<CheckList?>(null)
    val checkLists = _checkLists.asStateFlow()

    fun getList(userSession: UserSession) {
        viewModelScope.launch {
            val userId = userSession.user?.id ?: ""
            _checkLists.value = supabaseRepository.getCheckList(userId)
        }
    }

    fun addSection(section: Section) {
        upsertLocalSection(section)

        // Add the section to the database
        viewModelScope.launch {
            val newSection = supabaseRepository.addSection(section) ?: return@launch
            upsertLocalSection(newSection)
        }
    }

    private fun upsertLocalSection(section: Section) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        // check if section already exists with null id
        var combinedSection = section
        currentSections.find {
            it.checkEqualityWithNullId(section)
        }?.let { sectionMatch ->
            // combine items with the section to be deleted
            combinedSection = combinedSection.copy(
                items = combinedSection.items?.plus(sectionMatch.items.orEmpty())
            )
            currentSections.remove(sectionMatch)
        }
        // then add the new section
        currentSections.add(combinedSection)
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            supabaseRepository.updateSection(section)
        }
    }

    // This function is used to add an item to a section. If the section does not exist, it will be created.
    fun addItemToSection(section: Section, item: SectionItem) {
        var newItem = item.copy(sectionId = section.id)
        val isNewSection = upsertLocalItem(section, newItem)

        // Add the item to the database
        viewModelScope.launch {
            if (isNewSection) { // if the section is new, add the section first
                val newSection = supabaseRepository.addSection(section) ?: return@launch
                if (newSection.id == null) return@launch
                // add the new section to the local list
                upsertLocalSection(newSection)
                // set sectionId to be the new section's id
                newItem = newItem.copy(sectionId = newSection.id)
                // then, wait for the new item to be returned with its id
                newItem = supabaseRepository.addItem(newItem) ?: return@launch
                upsertLocalItem(newSection, newItem)
            } else { // otherwise, add the item to the existing section
                // first check if item already exists in database
                val currentItems = section.id?.let { supabaseRepository.getItems(it) }
                if (currentItems?.has(newItem) == true) return@launch
                // then, add the new item to the database
                newItem = supabaseRepository.addItem(newItem) ?: return@launch
                upsertLocalItem(section, newItem)
            }
        }
    }

    private fun upsertLocalItem(
        section: Section,
        item: SectionItem
    ): Boolean {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        val index = currentSections.indexOfFirst { it.id == section.id }

        return if (index != -1) {
            val currentItems = currentSections[index].items?.toMutableList() ?: mutableListOf()
            // remove the item if it already exists before id is set
            val itemIndex = currentItems.indexOfFirst {
                it.checkEqualityWithNullId(item)
            }

            if (itemIndex != -1) {
                val existingItem = currentItems[itemIndex]
                val itemCopy = if (existingItem.id != null) {
                    item.copy(id = existingItem.id, isChecked = existingItem.isChecked)
                } else {
                    item.copy(isChecked = existingItem.isChecked)
                }
                currentItems[itemIndex] = itemCopy
            } else {
                currentItems.add(item)
            }

            currentSections[index] = currentSections[index].copy(items = currentItems)
            _checkLists.update {
                it?.copy(
                    sections = currentSections
                )
            }
            false
        } else {
            val newSection = section.copy(items = listOf(item))
            currentSections.add(newSection)
            _checkLists.update {
                it?.copy(
                    sections = currentSections
                )
            }
            true
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            supabaseRepository.updateItem(item)
        }
    }

    fun signOut() {
        try {
            viewModelScope.launch {
                supabaseClient.auth.signOut()
            }
            println("Signed out")
        } catch (e: Exception) {
            println("Error during sign out: $e")
        }
    }
}
