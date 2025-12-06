package com.vlaados.freeze.data.model

data class User(
    val username: String,
    val role: String? = null,
    val telegram_id: String? = null,
    val name: String? = null,
    val income: Double? = 0.0,
    val weakness: String? = null,
    val goal_name: String? = null,
    val goal_amount: Double? = 0.0,
    val goal_date: String? = null,
    val saved_for_goal: Double? = 0.0,
    val two_factor_auth: Boolean? = false,
    val analytics_enabled: Boolean? = true,
    val communication_style: String? = null,
    val user_prompt: String? = null,
    val monthly_savings: Double? = 0.0,
    val self_ban: String? = null,
    val id: Int? = null,
    val registered_at: String? = null,
    val password: String? = null,
    val group_id: Int? = null
)
