package com.broccolist.checklist.data

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckListEntity(
    val id: Int? = null,
    @SerialName("created_at")
    val createdAt: String = Clock.System.now().toString(),
    @SerialName("edited_at")
    val editedAt: String = Clock.System.now().toString(),
    val name: String,
    @SerialName("user_id")
    val userId: String,
)
