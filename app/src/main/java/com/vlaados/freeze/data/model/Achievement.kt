package com.vlaados.freeze.data.model

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val photo_url: String?,
    val target_value: Int
)

data class UserAchievement(
    val id: Int,
    val user_id: Int,
    val achievement_id: Int,
    val current_value: Int
)
