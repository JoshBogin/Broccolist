package com.broccolist.checklist.presentation.components

import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem

/**
 * One row of the checklist's flat LazyColumn. Sections and their items are
 * flattened into a single list so an item can be dragged straight from one
 * section into another.
 */
internal sealed interface ChecklistRow {
    val key: String

    data class Header(val section: Section, val expanded: Boolean) : ChecklistRow {
        override val key: String = section.rowKey
    }

    data class Item(
        val item: SectionItem,
        val section: Section,
        val indexInSection: Int,
    ) : ChecklistRow {
        override val key: String = item.rowKey
    }

    /** The "Add Item" field at the bottom of an expanded section. */
    data class AddItem(val section: Section) : ChecklistRow {
        override val key: String = "add:${section.rowKey}"
    }
}

/**
 * Flattens [sections] into rows.
 *
 * @param collapsedKeys row keys of the sections the user has collapsed.
 * @param collapseAll show headers only; used while a section is being dragged.
 * @param draggingItemKey the item currently being dragged, if any. It stays
 *   visible even inside a collapsed section, because a dragged row that leaves
 *   the list would end the drag.
 */
internal fun buildChecklistRows(
    sections: List<Section>,
    collapsedKeys: Set<String>,
    collapseAll: Boolean,
    draggingItemKey: String?,
): List<ChecklistRow> = buildList {
    sections.forEach { section ->
        val expanded = !collapseAll && section.rowKey !in collapsedKeys
        add(ChecklistRow.Header(section, expanded))
        section.items.orEmpty().forEachIndexed { index, item ->
            if (expanded || item.rowKey == draggingItemKey) {
                add(ChecklistRow.Item(item, section, index))
            }
        }
        if (expanded) add(ChecklistRow.AddItem(section))
    }
}

internal data class ItemDrop(val itemId: Int, val sectionId: Int, val index: Int)

internal data class SectionDrop(val sectionId: Int, val index: Int)

/**
 * Works out where a dragged item lands when the row [fromKey] takes the place
 * of the row [toKey]. The item joins the section whose header ends up nearest
 * above it, at the index given by the item rows between that header and it.
 *
 * Returns null when the move isn't possible: either row is unsaved or unknown,
 * or the item would end up above the first header.
 */
internal fun resolveItemDrop(rows: List<ChecklistRow>, fromKey: Any, toKey: Any): ItemDrop? {
    val from = rows.indexOfFirst { it.key == fromKey }
    val to = rows.indexOfFirst { it.key == toKey }
    if (from == -1 || to == -1) return null
    val dragged = rows[from] as? ChecklistRow.Item ?: return null
    val itemId = dragged.item.id ?: return null

    val reordered = rows.toMutableList().apply { add(to, removeAt(from)) }
    val headerIndex = (to - 1 downTo 0).firstOrNull { reordered[it] is ChecklistRow.Header }
        ?: return null
    val header = reordered[headerIndex] as ChecklistRow.Header
    val sectionId = header.section.id ?: return null

    val index = if (header.expanded) {
        (headerIndex + 1 until to).count { reordered[it] is ChecklistRow.Item }
    } else {
        // A collapsed section shows none of its items, so there is no visible
        // slot to aim for: append.
        Int.MAX_VALUE
    }
    return ItemDrop(itemId, sectionId, index)
}

/**
 * Works out where a dragged section lands when the header [fromKey] takes the
 * place of the header [toKey]. Returns null if either isn't a saved section.
 */
internal fun resolveSectionDrop(rows: List<ChecklistRow>, fromKey: Any, toKey: Any): SectionDrop? {
    val headers = rows.filterIsInstance<ChecklistRow.Header>()
    val from = headers.firstOrNull { it.key == fromKey } ?: return null
    val to = headers.indexOfFirst { it.key == toKey }
    if (to == -1) return null
    return SectionDrop(from.section.id ?: return null, to)
}
