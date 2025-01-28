package com.broccolist.checklist.domain

data class CheckList(
    val id: Int,
    val name: String,
    val createdAt: String,
    val editedAt: String,
    val sections: List<Section>,
    val userId: String,
)
