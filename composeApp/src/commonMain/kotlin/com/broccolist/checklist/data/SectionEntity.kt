package com.broccolist.checklist.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SectionEntity(
    val id: Int? = null,
    val name: String,
    @SerialName("list_id")
    val listId: Int,
)
