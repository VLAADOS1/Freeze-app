package com.vlaados.freeze.data.model

data class CreateChallengeRequest(
    val name: String,
    val end_date: String?,
    val target_amount: Double?
)
