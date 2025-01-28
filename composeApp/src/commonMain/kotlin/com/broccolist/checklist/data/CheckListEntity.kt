package com.broccolist.checklist.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckListEntity(
    val id: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("edited_at")
    val editedAt: String,
    val name: String,
    @SerialName("user_id")
    val userId: String,
)
