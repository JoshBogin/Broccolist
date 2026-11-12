package com.broccolist.checklist.data

import com.broccolist.checklist.domain.CheckList
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock

class SupabaseRepositoryImpl(
    private val client: SupabaseClient
) : SupabaseRepository {
    override suspend fun getListsForUser(userId: String): List<CheckList> {
        return try {
            val listEntities = selectListsForUser(userId)
            val listIds = listEntities.mapNotNull { it.id }
            if (listIds.isEmpty()) return emptyList()

            // Three bulk queries (lists → sections → items) instead of one
            // per list / section, so the dashboard and Personal Lists screens
            // get real item counts without an N+1 fan-out.
            val sectionEntities = client.from("sections").select {
                filter { SectionEntity::listId isIn listIds }
            }.decodeList<SectionEntity>()

            val sectionIds = sectionEntities.mapNotNull { it.id }
            val itemsBySection = if (sectionIds.isEmpty()) {
                emptyMap()
            } else {
                client.from("items").select {
                    filter { ItemEntity::sectionId isIn sectionIds }
                }.decodeList<ItemEntity>().groupBy { it.sectionId }
            }

            val sectionsByList = sectionEntities.groupBy { it.listId }.mapValues { (_, entities) ->
                entities.map { entity ->
                    Section(
                        id = entity.id,
                        section = GrocerySection.fromString(entity.name),
                        items = itemsBySection[entity.id].orEmpty().map { it.toDomain() },
                        listId = entity.listId,
                    )
                }
            }

            listEntities.map { entity ->
                CheckList(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    editedAt = entity.editedAt,
                    sections = sectionsByList[entity.id].orEmpty(),
                    userId = entity.userId,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun ItemEntity.toDomain() = SectionItem(
        id = id,
        name = name,
        isChecked = isChecked,
        sectionId = sectionId,
    )

    override suspend fun getListById(listId: Int): CheckList? {
        return try {
            val entity = client.from("lists").select {
                filter { CheckListEntity::id eq listId }
            }.decodeList<CheckListEntity>().firstOrNull() ?: return null
            CheckList(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                editedAt = entity.editedAt,
                sections = getSections(entity.id ?: 0),
                userId = entity.userId,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createList(name: String, userId: String): CheckList? {
        return try {
            val inserted = client.from("lists").insert(
                CheckListEntity(name = name, userId = userId)
            ) { select() }.decodeSingle<CheckListEntity>()
            CheckList(
                id = inserted.id,
                name = inserted.name,
                createdAt = inserted.createdAt,
                editedAt = inserted.editedAt,
                sections = emptyList(),
                userId = inserted.userId,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun renameList(listId: Int, name: String) {
        try {
            client.from("lists").update(
                {
                    CheckListEntity::name setTo name
                    // Bumped client-side so "recently edited" ordering reflects
                    // the rename. Phase 3 replaces this with a server trigger.
                    CheckListEntity::editedAt setTo Clock.System.now().toString()
                }
            ) {
                filter { CheckListEntity::id eq listId }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteList(listId: Int) {
        try {
            client.from("lists").delete {
                filter { CheckListEntity::id eq listId }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun selectListsForUser(userId: String): List<CheckListEntity> {
        return client.from("lists").select {
            filter { CheckListEntity::userId eq userId }
            order("edited_at", Order.DESCENDING)
        }.decodeList<CheckListEntity>()
    }

    override suspend fun getSections(listId: Int): List<Section> {
        return try {
            client.from("sections").select {
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
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getItems(sectionId: Int): List<SectionItem> {
        return try {
            client.from("items").select {
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
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun addSection(section: Section): Section? {
        return try {
            client.from("sections").insert(
                SectionEntity(
                    name = section.section.displayName,
                    listId = section.listId,
                )
            ) {
                select()
            }.decodeSingle<SectionEntity>().let { entity ->
                Section(
                    id = entity.id,
                    section = section.section,
                    items = emptyList(),
                    listId = entity.listId
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun removeSection(section: Section) {
        try {
            client.from("sections").delete {
                filter {
                    SectionEntity::id eq section.id
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateSection(section: Section) {
        try {
            client.from("sections").update(
                {
                    SectionEntity::name setTo section.section.displayName
                }
            ) {
                filter {
                    SectionEntity::id eq section.id
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun addItem(item: SectionItem): SectionItem? {
        return try {
            client.from("items").insert(
                ItemEntity(
                    name = item.name,
                    isChecked = item.isChecked,
                    sectionId = item.sectionId
                )
            ) {
                select()
            }.decodeSingle<ItemEntity>().let { entity ->
                SectionItem(
                    id = entity.id,
                    name = entity.name,
                    isChecked = entity.isChecked,
                    sectionId = entity.sectionId
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun removeItem(item: SectionItem) {
        try {
            client.from("items").delete {
                filter {
                    ItemEntity::id eq item.id
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateItem(item: SectionItem) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
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
