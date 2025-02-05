package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem

interface SupabaseRepository {
    suspend fun createList(userId: String)
    suspend fun getCheckList(userId: String): CheckList?
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
