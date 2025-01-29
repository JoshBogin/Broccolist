package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.ListLoadingState
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class SupabaseRepositoryImpl(
    private val client: SupabaseClient
) : SupabaseRepository {
    private val _listItemsFlow =
        MutableStateFlow<ListLoadingState>(ListLoadingState.Empty)
    override val listItemsFlow: StateFlow<ListLoadingState> = _listItemsFlow

    override suspend fun createList(checkList: CheckList) {
        client.from("lists").insert(
            CheckListEntity(
                name = checkList.name,
                createdAt = checkList.createdAt,
                editedAt = checkList.editedAt,
                userId = checkList.userId
            )
        )
    }

    override suspend fun getCheckLists(): List<CheckList> {
        return client.from("lists").select().decodeList<CheckListEntity>().map { entity ->
            CheckList(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                editedAt = entity.editedAt,
                sections = getSections(entity.id ?: 0),
                userId = entity.userId,
            )
        }
    }

    suspend fun getCheckLists(userId: String) {
        _listItemsFlow.emit(ListLoadingState.Loading)

        var listResponse: List<CheckListEntity>? = null

        withContext(Dispatchers.IO) {
            val listCall = async {
                client.from("lists").select {
                    filter {
                        CheckListEntity::userId eq userId
                    }
                }
            }

            listResponse = listCall.await().decodeList<CheckListEntity>()
        }

        val list = listResponse?.map { entity ->
            CheckList(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                editedAt = entity.editedAt,
                sections = getSections(entity.id ?: 0),
                userId = entity.userId,
            )
        }
        if (list == null) {
            _listItemsFlow.emit(ListLoadingState.Error)
            return
        }
        _listItemsFlow.emit(ListLoadingState.Loaded(list))
    }

    override suspend fun getSections(id: Int): List<Section> {
        return client.from("sections").select {
            filter {
                SectionEntity::id eq id
            }
        }.decodeList<SectionEntity>().map { entity ->
            Section(
                id = entity.id,
                section = GrocerySection.fromString(entity.name),
                items = getItems(entity.id ?: 0),
                listId = entity.listId
            )
        }
    }

    override suspend fun getItems(sectionId: Int): List<SectionItem> {
        return client.from("items").select {
            filter {
                ItemEntity::id.eq(sectionId)
            }
        }.decodeList<ItemEntity>().map { entity ->
            SectionItem(
                id = entity.id,
                name = entity.name,
                isChecked = entity.isChecked,
                sectionId = entity.sectionId
            )
        }
    }

    override suspend fun addSection(section: Section) {
        client.from("sections").insert(
            SectionEntity(
                name = section.section.displayName,
                listId = section.listId,
            )
        )
    }

    override suspend fun removeSection(section: Section) {
        client.from("sections").delete {
            filter {
                SectionEntity::id.eq(section.id)
            }
        }
    }

    override suspend fun updateSection(section: Section) {
        client.from("sections").update(
            SectionEntity(
                id = section.id,
                name = section.section.displayName,
                listId = section.listId,
            )
        ) {
            filter {
                SectionEntity::id.eq(section.id)
            }
        }
    }

    override suspend fun addItem(item: SectionItem) {
        client.from("items").insert(
            ItemEntity(
                name = item.name,
                isChecked = item.isChecked,
                sectionId = item.sectionId
            )
        )
    }

    override suspend fun removeItem(item: SectionItem) {
        client.from("items").delete {
            filter {
                ItemEntity::id.eq(item.id)
            }
        }
    }

    // TODO utilize this function to save the checked state of an item
    override suspend fun updateItem(item: SectionItem) {
        client.from("items").update(
            ItemEntity(
                id = item.id,
                name = item.name,
                isChecked = item.isChecked,
                sectionId = item.sectionId
            )
        ) {
            filter {
                ItemEntity::id.eq(item.id)
            }
        }
    }

    override suspend fun addItemWithSection(item: SectionItem, section: Section) {
        val insertedSection = client.from("sections")
            .insert(
                SectionEntity(
                    name = section.section.displayName,
                    listId = section.listId
                )
            ){
                select()
            }.decodeSingle<SectionEntity>()

        client.from("items").insert(
            ItemEntity(
                name = item.name,
                isChecked = item.isChecked,
                sectionId = insertedSection.id ?: 0
            )
        )
    }
}
