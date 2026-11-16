package com.broccolist.core.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

/**
 * In-memory store for guest-mode list data. Phase 1 swaps the previous
 * "single list per guest" model for a real `List<CheckList>` so guests can
 * have multiple personal lists.
 *
 * This is still memory-only — Phase 3 will replace [InMemoryGuestListStore]
 * with a SQLDelight-backed implementation and add the migration step that
 * uploads everything when a guest signs up.
 *
 * IDs are hand-rolled negatives so they're easy to distinguish from
 * server-assigned positive IDs (which is useful for the Phase 3 migration
 * code).
 */
interface GuestListStore {
    val lists: StateFlow<List<CheckList>>

    fun createList(name: String): CheckList
    fun renameList(listId: Int, name: String)
    fun deleteList(listId: Int)
    fun getListById(listId: Int): CheckList?
    fun upsertSection(listId: Int, section: Section): Section
    fun removeSection(listId: Int, sectionId: Int)
    fun updateSection(listId: Int, section: Section)
    fun upsertItem(listId: Int, sectionId: Int, item: SectionItem): SectionItem
    fun removeItem(listId: Int, sectionId: Int, itemId: Int)
    fun updateItem(listId: Int, item: SectionItem)

    /** Clears all guest data; called on sign-out or after migration. */
    fun reset()
}

internal class InMemoryGuestListStore : GuestListStore {
    private val _lists = MutableStateFlow<List<CheckList>>(emptyList())
    override val lists: StateFlow<List<CheckList>> = _lists.asStateFlow()

    // Negative-int counter for local ID assignment. Each call decrements,
    // so allocations never collide and we can tell a guest row from a
    // synced row at a glance.
    private var idCounter: Int = -1
    private fun nextLocalId(): Int = idCounter--

    private fun now(): String = Clock.System.now().toString()

    override fun createList(name: String): CheckList {
        val list = CheckList(
            id = nextLocalId(),
            name = name,
            createdAt = now(),
            editedAt = now(),
            sections = emptyList(),
            userId = GUEST_USER_ID,
        )
        _lists.update { it + list }
        return list
    }

    override fun renameList(listId: Int, name: String) {
        _lists.update { lists ->
            lists.map {
                if (it.id == listId) it.copy(name = name, editedAt = now()) else it
            }
        }
    }

    override fun deleteList(listId: Int) {
        _lists.update { it.filter { list -> list.id != listId } }
    }

    override fun getListById(listId: Int): CheckList? =
        _lists.value.firstOrNull { it.id == listId }

    override fun upsertSection(listId: Int, section: Section): Section {
        val resolved = section.id?.let { section } ?: section.copy(id = nextLocalId())
        _lists.update { lists ->
            lists.map { list ->
                if (list.id != listId) return@map list
                val existing = list.sections.orEmpty().toMutableList()
                val idx = existing.indexOfFirst { it.id == resolved.id }
                if (idx == -1) existing.add(resolved) else existing[idx] = resolved
                list.copy(sections = existing, editedAt = now())
            }
        }
        return resolved
    }

    override fun removeSection(listId: Int, sectionId: Int) {
        _lists.update { lists ->
            lists.map { list ->
                if (list.id != listId) return@map list
                list.copy(
                    sections = list.sections.orEmpty().filter { it.id != sectionId },
                    editedAt = now(),
                )
            }
        }
    }

    override fun updateSection(listId: Int, section: Section) {
        if (section.id == null) return
        _lists.update { lists ->
            lists.map { list ->
                if (list.id != listId) return@map list
                list.copy(
                    sections = list.sections.orEmpty().map {
                        if (it.id == section.id) section else it
                    },
                    editedAt = now(),
                )
            }
        }
    }

    override fun upsertItem(listId: Int, sectionId: Int, item: SectionItem): SectionItem {
        val resolved = item.copy(
            id = item.id ?: nextLocalId(),
            sectionId = sectionId,
        )
        _lists.update { lists ->
            lists.map { list ->
                if (list.id != listId) return@map list
                list.copy(
                    sections = list.sections.orEmpty().map { section ->
                        if (section.id != sectionId) return@map section
                        val items = section.items.orEmpty().toMutableList()
                        val idx = items.indexOfFirst { it.id == resolved.id }
                        if (idx == -1) items.add(resolved) else items[idx] = resolved
                        section.copy(items = items)
                    },
                    editedAt = now(),
                )
            }
        }
        return resolved
    }

    override fun removeItem(listId: Int, sectionId: Int, itemId: Int) {
        _lists.update { lists ->
            lists.map { list ->
                if (list.id != listId) return@map list
                list.copy(
                    sections = list.sections.orEmpty().map { section ->
                        if (section.id != sectionId) return@map section
                        section.copy(items = section.items.orEmpty().filter { it.id != itemId })
                    },
                    editedAt = now(),
                )
            }
        }
    }

    override fun updateItem(listId: Int, item: SectionItem) {
        if (item.id == null) return
        _lists.update { lists ->
            lists.map { list ->
                if (list.id != listId) return@map list
                list.copy(
                    sections = list.sections.orEmpty().map { section ->
                        if (section.id != item.sectionId) return@map section
                        section.copy(items = section.items.orEmpty().map {
                            if (it.id == item.id) item else it
                        })
                    },
                    editedAt = now(),
                )
            }
        }
    }

    override fun reset() {
        _lists.value = emptyList()
        idCounter = -1
    }

    private companion object {
        const val GUEST_USER_ID = "guest"
    }
}
