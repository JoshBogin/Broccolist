package com.broccolist.checklist.domain

data class SectionItem(
    val id: Int? = null,
    val name: String,
    val isChecked: Boolean,
    val sectionId: Int,
) {
    fun checkEqualityWithNullId(other: SectionItem): Boolean {
        return name == other.name &&
            sectionId == other.sectionId
    }
}
