package com.broccolist.checklist.domain

data class CheckList(
    val id: Int? = null,
    val name: String,
    val createdAt: String? = null,
    val editedAt: String? = null,
    var sections: List<Section>,
    val userId: String,
)
