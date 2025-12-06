package com.vlaados.freeze.data.model

data class CreateGroupRequest(
    val name: String,
    val goal_name: String? = null,
    val goal_target_amount: Double? = null,
    val goal_date: String? = null
)
