package com.broccolist.checklist.domain

/**
 * Fractional ordering for sections and items.
 *
 * Every row carries a `position: Double`. Rows are shown in ascending
 * position; a moved row takes the midpoint of its new neighbours, so a drop
 * normally changes one row. When two neighbours get closer than [MIN_GAP]
 * there is no room left between them and the whole container is renumbered
 * 1, 2, 3, ...
 */
object Positioning {
    const val STEP = 1.0
    const val MIN_GAP = 1e-6

    /** Position for a row appended after [last] (null when the container is empty). */
    fun after(last: Double?): Double = (last ?: 0.0) + STEP

    /** Position for a row placed between two neighbours; null means "no neighbour on that side". */
    fun between(before: Double?, after: Double?): Double = when {
        before == null && after == null -> STEP
        before == null -> after!! - STEP
        after == null -> before + STEP
        else -> (before + after) / 2
    }

    /** True when there is no usable gap left between two neighbours. */
    fun isCrowded(before: Double?, after: Double?): Boolean =
        before != null && after != null && after - before < MIN_GAP
}

/**
 * Returns the sections with item [itemId] moved to index [toIndex] of section
 * [toSectionId]. [toIndex] counts the target section's items *without* the
 * moved item and is clamped, so any large value means "append".
 *
 * Returns the receiver unchanged when the item or target section is unknown.
 */
fun List<Section>.withItemMoved(itemId: Int, toSectionId: Int, toIndex: Int): List<Section> {
    val item = firstNotNullOfOrNull { section -> section.items?.firstOrNull { it.id == itemId } }
        ?: return this
    if (none { it.id == toSectionId }) return this

    return map { section ->
        val remaining = section.items.orEmpty().filter { it.id != itemId }
        when {
            section.id == toSectionId -> section.copy(
                items = remaining.inserting(
                    index = toIndex,
                    positionOf = { it.position },
                    place = { position -> item.copy(sectionId = toSectionId, position = position) },
                    renumber = { other, position -> other.copy(position = position) },
                ),
            )
            remaining.size != section.items.orEmpty().size -> section.copy(items = remaining)
            else -> section
        }
    }
}

/**
 * Returns the sections with section [sectionId] moved to index [toIndex].
 * [toIndex] counts the list *without* the moved section and is clamped.
 */
fun List<Section>.withSectionMoved(sectionId: Int, toIndex: Int): List<Section> {
    val section = firstOrNull { it.id == sectionId } ?: return this
    return filter { it.id != sectionId }.inserting(
        index = toIndex,
        positionOf = { it.position },
        place = { position -> section.copy(position = position) },
        renumber = { other, position -> other.copy(position = position) },
    )
}

/**
 * Inserts a row at [index] of a position-sorted list. The new row gets the
 * midpoint of its neighbours; if they are too close together every row is
 * renumbered instead.
 */
private fun <T> List<T>.inserting(
    index: Int,
    positionOf: (T) -> Double,
    place: (Double) -> T,
    renumber: (T, Double) -> T,
): List<T> {
    val at = index.coerceIn(0, size)
    val before = getOrNull(at - 1)?.let(positionOf)
    val after = getOrNull(at)?.let(positionOf)

    val result = toMutableList()
    result.add(at, place(Positioning.between(before, after)))
    // Crowded also covers ties, which rows written without a position leave
    // behind (the column default is 0).
    return if (Positioning.isCrowded(before, after)) {
        result.mapIndexed { i, row -> renumber(row, (i + 1) * Positioning.STEP) }
    } else {
        result
    }
}
