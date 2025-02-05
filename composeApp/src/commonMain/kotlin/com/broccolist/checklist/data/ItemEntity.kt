package com.broccolist.checklist.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemEntity(
    val id: Int? = null,
    val name: String,
    @SerialName("is_checked")
    val isChecked: Boolean,
    @SerialName("section_id")
    val sectionId: Int? = null,
)
