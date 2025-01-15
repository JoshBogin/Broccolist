package com.broccolist.models

data class Section(
    val name: String,
    val items: List<String> = emptyList() // this should maybe be a data class too that has a name and isChecked
)
