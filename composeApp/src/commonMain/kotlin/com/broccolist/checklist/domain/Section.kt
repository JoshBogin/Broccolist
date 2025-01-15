package com.broccolist.checklist.domain

data class Section(
    val id: String,
    val name: String,
    val items: List<SectionItem>?
)

data class SectionItem(
    val name: String,
    val isChecked: Boolean
)
