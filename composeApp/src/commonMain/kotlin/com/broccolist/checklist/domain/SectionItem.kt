package com.broccolist.checklist.domain

data class SectionItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean,
    val sectionId: Int,
)
