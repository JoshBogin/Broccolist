package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.ListLoadingState
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import kotlinx.coroutines.flow.StateFlow

interface SupabaseRepository {
    val listItemsFlow: StateFlow<ListLoadingState>

    suspend fun createList(checkList: CheckList)
    suspend fun getCheckLists(): List<CheckList>
    suspend fun getSections(id: Int): List<Section>
    suspend fun getItems(sectionId: Int): List<SectionItem>
    suspend fun addSection(section: Section)
    suspend fun removeSection(section: Section)
    suspend fun updateSection(section: Section)
    suspend fun addItem(item: SectionItem)
    suspend fun removeItem(item: SectionItem)
    suspend fun updateItem(item: SectionItem)
    suspend fun addItemWithSection(item: SectionItem, section: Section)
}
