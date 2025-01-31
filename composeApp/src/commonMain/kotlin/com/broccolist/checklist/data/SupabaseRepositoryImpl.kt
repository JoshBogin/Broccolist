package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseRepositoryImpl(
    private val client: SupabaseClient
) : SupabaseRepository {
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

    override suspend fun getCheckList(userId: String): CheckList {
        return client.from("lists").select {
            filter {
                CheckListEntity::userId eq userId
            }
        }.decodeSingle<CheckListEntity>().let { entity ->
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

    override suspend fun getSections(listId: Int): List<Section> {
        return client.from("sections").select {
            filter {
                SectionEntity::listId eq listId
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
                ItemEntity::sectionId eq sectionId
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

    override suspend fun addSection(section: Section): Section {
        client.from("sections").insert(
            SectionEntity(
                name = section.section.displayName,
                listId = section.listId,
            )
        ) {
            select()
        }.decodeSingle<SectionEntity>().let { entity ->
            return Section(
                id = entity.id,
                section = section.section,
                items = emptyList(),
                listId = entity.listId
            )
        }
    }

    override suspend fun removeSection(section: Section) {
        client.from("sections").delete {
            filter {
                SectionEntity::id eq section.id
            }
        }
    }

    override suspend fun updateSection(section: Section) {
        client.from("sections").update(
            {
                SectionEntity::name setTo section.section.displayName
            }
        ) {
            filter {
                SectionEntity::id eq section.id
            }
        }
    }

    override suspend fun addItem(item: SectionItem): SectionItem {
        client.from("items").insert(
            ItemEntity(
                name = item.name,
                isChecked = item.isChecked,
                sectionId = item.sectionId
            )
        ) {
            select()
        }.decodeSingle<ItemEntity>().let { entity ->
            return SectionItem(
                id = entity.id,
                name = entity.name,
                isChecked = entity.isChecked,
                sectionId = entity.sectionId
            )
        }
    }

    override suspend fun removeItem(item: SectionItem) {
        client.from("items").delete {
            filter {
                ItemEntity::id eq item.id
            }
        }
    }

    override suspend fun updateItem(item: SectionItem) {
        client.from("items").update(
            {
                ItemEntity::name setTo item.name
                ItemEntity::isChecked setTo item.isChecked
            }
        ) {
            filter {
                ItemEntity::id eq item.id
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
            ) {
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
