package com.broccolist.checklist.domain

data class SectionItem(
    val id: Int? = null,
    val name: String,
    val isChecked: Boolean,
    val sectionId: Int? = null,
    /** Sort key within the section; see [Positioning]. */
    val position: Double = 0.0,
    /** Client-only stable identity; see [Section.clientKey]. Never persisted. */
    val clientKey: String? = null,
) {
    fun checkEqualityWithNullId(other: SectionItem): Boolean {
        return name == other.name
    }
}

fun List<SectionItem>.has(item: SectionItem): Boolean {
    return this.any { it.checkEqualityWithNullId(item) }
}
