package com.broccolist.checklist.domain

data class SectionItem(
    val id: Int? = null,
    val name: String,
    val isChecked: Boolean,
    val sectionId: Int? = null,
) {
    fun checkEqualityWithNullId(other: SectionItem): Boolean {
        return name == other.name
    }
}

fun List<SectionItem>.has(item: SectionItem): Boolean {
    return this.any { it.checkEqualityWithNullId(item) }
}
