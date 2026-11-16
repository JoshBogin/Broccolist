package com.broccolist.checklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Positioning
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import com.broccolist.checklist.domain.has
import com.broccolist.checklist.domain.withItemMoved
import com.broccolist.checklist.domain.withSectionMoved
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
        val positioned = section.asNewSectionIfUnsaved()
        if (isGuest) {
            // The store assigns the local id (one counter for the whole guest
            // session, so ids never collide across screens) and keeps the
            // dashboard's counts/preview in sync.
            upsertLocalSection(guestListStore.upsertSection(listId, positioned))
            return
        }
        upsertLocalSection(positioned)

        viewModelScope.launch {
            val newSection = supabaseRepository.addSection(positioned) ?: return@launch
            upsertLocalSection(newSection)
        }
    }

    /** A section that hasn't been saved yet goes to the end of the list. */
    private fun Section.asNewSectionIfUnsaved(): Section {
        if (id != null) return this
        val last = _checkLists.value?.sections?.maxOfOrNull { it.position }
        // Guest rows get their id straight away and outlive this ViewModel
        // in GuestListStore, so they don't need (and mustn't keep) a client key.
        return copy(
            position = Positioning.after(last),
            clientKey = if (isGuest) null else nextClientKey(),
        )
    }

    private fun upsertLocalSection(section: Section) {
        val currentSections = _checkLists.value?.sections?.toMutableList() ?: mutableListOf()
        // check if section already exists with null id
        var combinedSection = section
        currentSections.find {
            it.checkEqualityWithNullId(section)
        }?.let { sectionMatch ->
            // combine items with the section to be deleted, and keep its
            // client key so the UI row survives the id arriving
            combinedSection = combinedSection.copy(
                items = combinedSection.items?.plus(sectionMatch.items.orEmpty()),
                clientKey = sectionMatch.clientKey ?: combinedSection.clientKey,
            )
            currentSections.remove(sectionMatch)
        }
        // then add the new section, keeping the list in display order
        currentSections.add(combinedSection)
        _checkLists.update {
            it?.copy(
                sections = currentSections.sortedBy { section -> section.position }
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
            // Ids come from the store, whose counter spans the whole guest
            // session.
            val storedSection = if (section.id == null) {
                guestListStore.upsertSection(listId, section.asNewSectionIfUnsaved())
            } else {
                section
            }
            val localItem = item.copy(
                id = null,
                sectionId = storedSection.id,
                position = nextItemPosition(storedSection.id),
            )
            val storedItem = guestListStore.upsertItem(listId, storedSection.id ?: 0, localItem)
            upsertLocalItem(storedSection, storedItem)
            return
        }

        val target = section.asNewSectionIfUnsaved()
        var newItem = item.copy(
            sectionId = target.id,
            position = nextItemPosition(target.id),
            clientKey = nextClientKey(),
        )
        val isNewSection = upsertLocalItem(target, newItem)

        // Add the item to the database
        viewModelScope.launch {
            if (isNewSection) { // if the section is new, add the section first
                val newSection = supabaseRepository.addSection(target) ?: return@launch
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
                val currentItems = target.id?.let { supabaseRepository.getItems(it) }
                if (currentItems?.has(newItem) == true) return@launch
                // then, add the new item to the database
                newItem = supabaseRepository.addItem(newItem) ?: return@launch
                upsertLocalItem(target, newItem)
            }
        }
    }

    /** Position that appends an item to the end of section [sectionId]. */
    private fun nextItemPosition(sectionId: Int?): Double {
        val items = _checkLists.value?.sections?.firstOrNull { it.id == sectionId }?.items
        return Positioning.after(items?.maxOfOrNull { it.position })
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
                // The row keeps its place and its UI identity; only the
                // server-assigned fields are taken from the incoming item.
                val existingItem = currentItems[itemIndex]
                currentItems[itemIndex] = item.copy(
                    id = existingItem.id ?: item.id,
                    isChecked = existingItem.isChecked,
                    position = existingItem.position,
                    clientKey = existingItem.clientKey ?: item.clientKey,
                )
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

    /**
     * Sections as they were when the current drag started. Moves only touch
     * local state while the row is in the air; [commitReorder] diffs against
     * this to work out what to persist.
     */
    private var reorderSnapshot: List<Section>? = null

    /** Move an item to [toIndex] of section [toSectionId]; local only until [commitReorder]. */
    fun moveItem(itemId: Int, toSectionId: Int, toIndex: Int) {
        reorder { it.withItemMoved(itemId, toSectionId, toIndex) }
    }

    /** Move a section to [toIndex] of the list; local only until [commitReorder]. */
    fun moveSection(sectionId: Int, toIndex: Int) {
        reorder { it.withSectionMoved(sectionId, toIndex) }
    }

    private fun reorder(transform: (List<Section>) -> List<Section>) {
        val current = _checkLists.value?.sections ?: return
        if (reorderSnapshot == null) reorderSnapshot = current
        _checkLists.update { it?.copy(sections = transform(current)) }
    }

    /**
     * Persist the outcome of a drag. Normally that is one row (the one that
     * was dropped); a renumbered section or list writes every row in it.
     */
    fun commitReorder() {
        val before = reorderSnapshot ?: return
        reorderSnapshot = null
        val after = _checkLists.value?.sections ?: return

        val oldSectionPositions = before.associate { it.id to it.position }
        val movedSections = after.filter { section ->
            val old = oldSectionPositions[section.id]
            section.id != null && old != null && old != section.position
        }
        val oldItems = before.flatMap { it.items.orEmpty() }.associateBy { it.id }
        val movedItems = after.flatMap { it.items.orEmpty() }.filter { item ->
            val old = oldItems[item.id]
            item.id != null && old != null &&
                (old.sectionId != item.sectionId || old.position != item.position)
        }
        if (movedSections.isEmpty() && movedItems.isEmpty()) return

        if (isGuest) {
            movedSections.forEach { guestListStore.moveSection(listId, it.id ?: return@forEach, it.position) }
            movedItems.forEach { item ->
                guestListStore.moveItem(
                    listId = listId,
                    itemId = item.id ?: return@forEach,
                    sectionId = item.sectionId ?: return@forEach,
                    position = item.position,
                )
            }
            return
        }

        viewModelScope.launch {
            movedSections.forEach { supabaseRepository.moveSection(it.id ?: return@forEach, it.position) }
            movedItems.forEach { item ->
                supabaseRepository.moveItem(
                    itemId = item.id ?: return@forEach,
                    sectionId = item.sectionId ?: return@forEach,
                    position = item.position,
                )
            }
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

    /** Source of [Section.clientKey] / [SectionItem.clientKey] values. */
    private var clientKeyCounter: Int = 0
    private fun nextClientKey(): String = "c${clientKeyCounter++}"
}
