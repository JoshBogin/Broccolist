package com.broccolist.checklist.presentation.components

import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChecklistRowsTest {
    private fun item(id: Int?, sectionId: Int, name: String = "item$id") =
        SectionItem(id = id, name = name, isChecked = false, sectionId = sectionId)

    private fun section(id: Int, vararg items: SectionItem) =
        Section(id = id, section = GrocerySection.Custom("section$id"), items = items.toList(), listId = 1)

    private val sections = listOf(
        section(10, item(1, 10), item(2, 10)),
        section(20, item(3, 20)),
        section(30),
    )

    private fun rows(
        collapsed: Set<String> = emptySet(),
        collapseAll: Boolean = false,
        dragging: String? = null,
    ) = buildChecklistRows(sections, collapsed, collapseAll, dragging)

    @Test
    fun flattensHeadersItemsAndAddRows() {
        assertEquals(
            listOf(
                "section:10", "item:1", "item:2", "add:section:10",
                "section:20", "item:3", "add:section:20",
                "section:30", "add:section:30",
            ),
            rows().map { it.key },
        )
    }

    @Test
    fun collapsedSectionShowsOnlyItsHeaderPlusTheDraggedItem() {
        assertEquals(
            listOf("section:10", "section:20", "item:3", "add:section:20", "section:30", "add:section:30"),
            rows(collapsed = setOf("section:10")).map { it.key },
        )
        assertEquals(
            listOf("section:10", "item:2", "section:20", "section:30"),
            rows(collapseAll = true, dragging = "item:2").map { it.key },
        )
    }

    @Test
    fun clientKeyWinsOverIdSoTheKeySurvivesTheInsertReturning() {
        val pending = item(null, 10, "milk").copy(clientKey = "c0")
        val saved = pending.copy(id = 42)
        assertEquals(pending.rowKey, saved.rowKey)
    }

    @Test
    fun dropWithinSection() {
        // item 2 takes item 1's place
        assertEquals(ItemDrop(2, 10, 0), resolveItemDrop(rows(), "item:2", "item:1"))
        // item 1 takes item 2's place
        assertEquals(ItemDrop(1, 10, 1), resolveItemDrop(rows(), "item:1", "item:2"))
    }

    @Test
    fun draggingDownPastAHeaderEntersThatSectionAtTheTop() {
        assertEquals(ItemDrop(2, 20, 0), resolveItemDrop(rows(), "item:2", "section:20"))
    }

    @Test
    fun draggingUpPastAHeaderLandsAtTheEndOfTheSectionAbove() {
        assertEquals(ItemDrop(3, 10, 2), resolveItemDrop(rows(), "item:3", "section:20"))
    }

    @Test
    fun cannotDropAboveTheFirstHeader() {
        assertNull(resolveItemDrop(rows(), "item:1", "section:10"))
    }

    @Test
    fun droppingIntoACollapsedSectionAppends() {
        val collapsed = rows(collapsed = setOf("section:20"))
        assertEquals(ItemDrop(2, 20, Int.MAX_VALUE), resolveItemDrop(collapsed, "item:2", "section:20"))
    }

    @Test
    fun unsavedOrUnknownRowsCannotBeDropped() {
        val withPending = buildChecklistRows(
            listOf(section(10, item(1, 10), item(null, 10, "pending"))),
            emptySet(),
            collapseAll = false,
            draggingItemKey = null,
        )
        assertNull(resolveItemDrop(withPending, "item:pending:10:pending", "item:1"))
        assertNull(resolveItemDrop(rows(), "item:99", "item:1"))
        assertNull(resolveItemDrop(rows(), "section:10", "item:1"))
    }

    @Test
    fun sectionDropUsesHeaderOrder() {
        val headersOnly = rows(collapseAll = true)
        assertEquals(SectionDrop(30, 0), resolveSectionDrop(headersOnly, "section:30", "section:10"))
        assertEquals(SectionDrop(10, 2), resolveSectionDrop(headersOnly, "section:10", "section:30"))
        assertNull(resolveSectionDrop(headersOnly, "section:10", "item:1"))
    }
}
