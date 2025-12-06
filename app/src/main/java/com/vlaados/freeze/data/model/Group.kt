package com.vlaados.freeze.data.model

data class Group(
    val id: Int,
    val name: String,
    val code: String,
    val users: List<User>?,
    val goal_name: String?,
    val goal_target_amount: Double?,
    val goal_date: String?
)

data class SharedGoal(
    val id: Int,
    val name: String,
    val amount: Double,
    val date: String,
    val current_amount: Double? = 0.0
)

data class GroupMember(
    val user_id: Int,
    val username: String,
    val name: String?,
    val saved_for_group: Double?
)


