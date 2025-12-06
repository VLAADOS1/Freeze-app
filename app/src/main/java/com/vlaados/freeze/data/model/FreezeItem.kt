package com.vlaados.freeze.data.model

data class FreezeItem(
    val item_name: String,
    val start_time: String,
    val duration_seconds: Int,
    val is_frozen: Boolean? = true,
    val id: Int = 0,
    val user_id: Int = 0,
    val amount: Double? = 0.0
)
