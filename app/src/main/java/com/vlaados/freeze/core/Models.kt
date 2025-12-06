package com.vlaados.freeze.core

import java.util.UUID

data class User(
    val name: String,
    val salary: Int? = null,
    val weaknesses: String? = null,
    val goal: String? = null
)

data class Purchase(
    val name: String,
    val price: Int,
    val comment: String? = null,
    val id: String = UUID.randomUUID().toString()
)

data class FrozenPurchase(
    val purchase: Purchase,
    val freezeUntil: Long,
    val freezeStartedTimestamp: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString()
)