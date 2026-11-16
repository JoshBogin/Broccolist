package com.broccolist.checklist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PositioningTest {
    private fun item(id: Int, sectionId: Int, position: Double) =
        SectionItem(id = id, name = "item$id", isChecked = false, sectionId = sectionId, position = position)

    private fun section(id: Int, position: Double, vararg items: SectionItem) =
        Section(
            id = id,
            section = GrocerySection.Custom("section$id"),
            items = items.toList(),
            listId = 1,
            position = position,
        )

    private val sections = listOf(
        section(10, 1.0, item(1, 10, 1.0), item(2, 10, 2.0), item(3, 10, 3.0)),
        section(20, 2.0, item(4, 20, 1.0)),
        section(30, 3.0),
    )

    private fun List<Section>.itemIds(sectionId: Int) =
        first { it.id == sectionId }.items.orEmpty().map { it.id }

    private fun List<Section>.item(id: Int) =
        flatMap { it.items.orEmpty() }.first { it.id == id }

    @Test
    fun betweenHandlesMissingNeighbours() {
        assertEquals(1.0, Positioning.between(null, null))
        assertEquals(4.0, Positioning.between(null, 5.0))
        assertEquals(6.0, Positioning.between(5.0, null))
        assertEquals(1.5, Positioning.between(1.0, 2.0))
        assertEquals(1.0, Positioning.after(null))
        assertEquals(4.0, Positioning.after(3.0))
    }

    @Test
    fun movesItemWithinSectionChangingOnlyThatItem() {
        val moved = sections.withItemMoved(itemId = 3, toSectionId = 10, toIndex = 0)

        assertEquals(listOf(3, 1, 2), moved.itemIds(10))
        assertEquals(0.0, moved.item(3).position)
        assertEquals(sections.item(1), moved.item(1))
        assertEquals(sections.item(2), moved.item(2))
    }

    @Test
    fun movesItemBetweenNeighbours() {
        val moved = sections.withItemMoved(itemId = 1, toSectionId = 10, toIndex = 1)

        assertEquals(listOf(2, 1, 3), moved.itemIds(10))
        assertEquals(2.5, moved.item(1).position)
    }

    @Test
    fun movesItemAcrossSections() {
        val moved = sections.withItemMoved(itemId = 2, toSectionId = 20, toIndex = 0)

        assertEquals(listOf(1, 3), moved.itemIds(10))
        assertEquals(listOf(2, 4), moved.itemIds(20))
        assertEquals(20, moved.item(2).sectionId)
        assertEquals(0.0, moved.item(2).position)
    }

    @Test
    fun oversizedIndexAppends() {
        val intoEmpty = sections.withItemMoved(itemId = 1, toSectionId = 30, toIndex = Int.MAX_VALUE)
        assertEquals(listOf(1), intoEmpty.itemIds(30))
        assertEquals(1.0, intoEmpty.item(1).position)

        val toEnd = sections.withItemMoved(itemId = 1, toSectionId = 10, toIndex = Int.MAX_VALUE)
        assertEquals(listOf(2, 3, 1), toEnd.itemIds(10))
        assertEquals(4.0, toEnd.item(1).position)
    }

    @Test
    fun unknownItemOrSectionIsANoOp() {
        assertSame(sections, sections.withItemMoved(itemId = 99, toSectionId = 10, toIndex = 0))
        assertSame(sections, sections.withItemMoved(itemId = 1, toSectionId = 99, toIndex = 0))
        assertSame(sections, sections.withSectionMoved(sectionId = 99, toIndex = 0))
    }

    @Test
    fun renumbersWhenNeighboursAreTied() {
        val tied = listOf(
            section(10, 1.0, item(1, 10, 0.0), item(2, 10, 0.0), item(3, 10, 5.0)),
        )

        val moved = tied.withItemMoved(itemId = 3, toSectionId = 10, toIndex = 1)

        assertEquals(listOf(1, 3, 2), moved.itemIds(10))
        assertEquals(listOf(1.0, 2.0, 3.0), moved.first().items.orEmpty().map { it.position })
    }

    @Test
    fun repeatedMovesKeepPositionsStrictlyIncreasing() {
        var current = sections
        repeat(200) { step ->
            // Keep dropping the last item into the second slot, which halves
            // the same gap every time until a renumber is forced.
            val last = current.itemIds(10).last()!!
            current = current.withItemMoved(itemId = last, toSectionId = 10, toIndex = 1)
            val positions = current.first().items.orEmpty().map { it.position }
            assertTrue(
                positions.zipWithNext().all { (a, b) -> b - a >= Positioning.MIN_GAP / 2 },
                "positions collapsed at step $step: $positions",
            )
        }
    }

    @Test
    fun movesSection() {
        val moved = sections.withSectionMoved(sectionId = 30, toIndex = 0)
        assertEquals(listOf(30, 10, 20), moved.map { it.id })
        assertEquals(0.0, moved.first().position)

        val middle = sections.withSectionMoved(sectionId = 10, toIndex = 1)
        assertEquals(listOf(20, 10, 30), middle.map { it.id })
        assertEquals(2.5, middle[1].position)
        // Items travel with their section untouched.
        assertEquals(sections.first().items, middle[1].items)
    }
}
