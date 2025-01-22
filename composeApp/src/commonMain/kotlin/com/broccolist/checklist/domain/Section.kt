package com.broccolist.checklist.domain

data class Section(
    val id: String,
    val section: GrocerySection,
    val items: List<SectionItem>?
)

data class SectionItem(
    val id: String,
    val name: String,
    val isChecked: Boolean
)
