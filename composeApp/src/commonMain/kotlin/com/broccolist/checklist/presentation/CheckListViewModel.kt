package com.broccolist.checklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import com.broccolist.checklist.domain.has
import com.broccolist.core.data.GuestListStore
import com.broccolist.core.data.GuestModeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckListViewModel(
    private val supabaseClient: SupabaseClient,
    private val supabaseRepository: SupabaseRepository,
    private val guestModeRepository: GuestModeRepository,
    private val guestListStore: GuestListStore,
) : ViewModel() {
    private val _checkLists = MutableStateFlow<CheckList?>(null)
    val checkLists = _checkLists.asStateFlow()

    /** The id of the list currently bound to this ViewModel. */
    private var listId: Int = 0

    /** True when the current data set is local-only (guest mode). */
    private val isGuest: Boolean get() = guestModeRepository.isGuest.value

    /**
     * Load a specific list by id. Guests pull from [GuestListStore]; signed-in
     * users fetch the list (including sections + items) from Supabase. Called
     * once on screen entry from [CheckListScreenRoute].
     */
    fun loadList(listId: Int) {
        this.listId = listId
        if (isGuest) {
            _checkLists.value = guestListStore.getListById(listId)
            return
        }
        viewModelScope.launch {
            _checkLists.value = supabaseRepository.getListById(listId)
        }
    }

    fun addSection(section: Section) {
        // Optimistic local update covers both guest and signed-in users.
        // Guests never sync; signed-in users still get the DB write below.
        val localSection = if (isGuest && section.id == null) {
            section.copy(id = nextLocalId())
        } else {
            section
        }
        upsertLocalSection(localSection)
        if (isGuest) {
            // Mirror to the store so the dashboard's counts/preview update.
            guestListStore.upsertSection(listId, localSection)
            return
        }

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
        if (isGuest) {
            guestListStore.removeSection(listId, section.id)
            return
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
        if (isGuest) {
            guestListStore.updateSection(listId, section)
            return
        }

        // Update the section in the database
        viewModelScope.launch {
            supabaseRepository.updateSection(section)
        }
    }

    // This function is used to add an item to a section. If the section does not exist, it will be created.
    fun addItemToSection(section: Section, item: SectionItem) {
        if (isGuest) {
            // Guest path: assign a local id, ensure the section exists locally,
            // and insert the item. No network round-trip.
            val resolvedSection = section.id?.let { section } ?: section.copy(id = nextLocalId())
            val storedSection = guestListStore.upsertSection(listId, resolvedSection)
            val localItem = item.copy(
                id = nextLocalId(),
                sectionId = storedSection.id,
            )
            val storedItem = guestListStore.upsertItem(listId, storedSection.id ?: 0, localItem)
            upsertLocalItem(storedSection, storedItem)
            return
        }

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
        if (isGuest) {
            guestListStore.removeItem(listId, section.id, item.id)
            return
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
        if (isGuest) {
            guestListStore.updateItem(listId, item)
            return
        }

        // Update the item in the database
        viewModelScope.launch {
            supabaseRepository.updateItem(item)
        }
    }

    fun signOut() {
        // Drop any in-memory guest data when leaving the session. Phase 3 will
        // also clear the local SQLDelight DB (or migrate it to the account
        // the user is signing into, depending on flow).
        if (isGuest) {
            guestListStore.reset()
            guestModeRepository.exitGuestMode()
            _checkLists.value = null
            return
        }
        try {
            viewModelScope.launch {
                supabaseClient.auth.signOut()
            }
            println("Signed out")
        } catch (e: Exception) {
            println("Error during sign out: $e")
        }
    }

    /**
     * Hand-rolled local ID generator for guest-mode sections and items. Uses
     * negative numbers so they're easy to distinguish from server-assigned
     * positive IDs. Will be replaced by SQLDelight auto-increment in Phase 3.
     */
    private var localIdCounter: Int = -1
    private fun nextLocalId(): Int = localIdCounter--
}
