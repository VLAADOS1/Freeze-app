package com.vlaados.freeze.data.model

data class Saving(
    val item_name: String,
    val date: String,
    val amount: Double,
    val is_breakdown: Boolean,
    val id: Int,
    val user_id: Int
)
