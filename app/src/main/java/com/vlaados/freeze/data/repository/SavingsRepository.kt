package com.vlaados.freeze.data.repository

import com.vlaados.freeze.data.model.Saving

interface SavingsRepository {
    suspend fun getSavings(token: String): Result<List<Saving>>
}
