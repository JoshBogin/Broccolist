package com.broccolist.checklist.domain

data class Section(
    val id: Int? = null,
    val section: GrocerySection,
    val items: List<SectionItem>?,
    val listId: Int,
)
