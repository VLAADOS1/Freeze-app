package com.vlaados.freeze.data.remote

data class TokenResponse(val access_token: String, val token_type: String)

data class User(
    val username: String,
    val role: String,
    val telegram_id: String? = null,
    val name: String? = null,
    val income: Float = 0.0f,
    val weakness: String? = null,
    val goal_name: String? = null,
    val goal_amount: Float = 0.0f,
    val goal_date: String,
    val saved_for_goal: Float = 0.0f,
    val two_factor_auth: Boolean = false,
    val analytics_enabled: Boolean = true,
    val communication_style: String? = null,
    val user_prompt: String? = null,
    val monthly_savings: Float = 0.0f,
    val self_ban: String? = null,
    val id: Int,
    val registered_at: String
)
