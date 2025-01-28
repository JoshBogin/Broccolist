package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class CheckListRepositoryImpl(
    private val client: SupabaseClient
) : CheckListRepository {
    override suspend fun createList(checkList: CheckList) {
        client.from("lists").insert(
            CheckListEntity(
                id = checkList.id,
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
                sections = getSections(entity.id),
                userId = entity.userId,
            )
        }
    }

    override suspend fun getSections(id: Int): List<Section> {
        return client.from("sections").select {
            filter {
                SectionEntity::id.eq(id)
            }
        }.decodeList<SectionEntity>().map { entity ->
            Section(
                id = entity.id,
                section = GrocerySection.fromString(entity.name),
                items = getItems(entity.id),
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
                id = section.id,
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
                id = item.id,
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
}
