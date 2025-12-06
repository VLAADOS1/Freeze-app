package com.vlaados.freeze.data.model

data class ChallengeData(
    val id: Int,
    val name: String,
    val end_date: String?,
    val target_amount: Double?,
    val participants: List<GroupMember>? = null
)
