package com.broccolist.checklist.domain

data class Section(
    val id: Int,
    val section: GrocerySection,
    val items: List<SectionItem>?,
    val listId: Int,
)
