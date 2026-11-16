package com.broccolist.checklist.domain

data class Section(
    val id: Int? = null,
    val section: GrocerySection,
    val items: List<SectionItem>?,
    val listId: Int,
    /** Sort key within the list; see [Positioning]. */
    val position: Double = 0.0,
    /**
     * Client-only identity assigned when the section is created on this
     * device. It survives the moment the insert returns and [id] flips from
     * null to the server id, so the UI can key rows on it without recreating
     * them mid-edit. Never persisted.
     */
    val clientKey: String? = null,
) {
    fun checkEqualityWithNullId(other: Section): Boolean {
        return section == other.section &&
            listId == other.listId
    }
}

fun List<Section>.has(section: GrocerySection): Boolean {
    return this.any { it.section == section }
}
