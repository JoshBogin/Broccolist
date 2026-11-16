package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem

interface SupabaseRepository {
    /** All personal lists owned by [userId], freshest first. */
    suspend fun getListsForUser(userId: String): List<CheckList>

    /** Load a single list by id, including all sections + items. */
    suspend fun getListById(listId: Int): CheckList?

    /** Insert a new list with the supplied name; returns the persisted row. */
    suspend fun createList(name: String, userId: String): CheckList?

    /** Rename an existing list and bump its `edited_at`. */
    suspend fun renameList(listId: Int, name: String)

    /** Delete a list; cascading deletes wipe its sections and items. */
    suspend fun deleteList(listId: Int)

    suspend fun getSections(listId: Int): List<Section>
    suspend fun getItems(sectionId: Int): List<SectionItem>
    suspend fun addSection(section: Section): Section?
    suspend fun removeSection(section: Section)
    suspend fun updateSection(section: Section)
    suspend fun addItem(item: SectionItem): SectionItem?
    suspend fun removeItem(item: SectionItem)
    suspend fun updateItem(item: SectionItem)
    suspend fun addItemWithSection(item: SectionItem, section: Section)
}
