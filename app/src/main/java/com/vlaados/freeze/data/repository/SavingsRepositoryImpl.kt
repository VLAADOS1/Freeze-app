package com.vlaados.freeze.data.repository

import com.vlaados.freeze.data.model.Saving
import com.vlaados.freeze.data.remote.ApiService
import javax.inject.Inject

class SavingsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SavingsRepository {
    override suspend fun getSavings(token: String): Result<List<Saving>> {
        return try {
            val savings = apiService.getSavings("Bearer $token")
            Result.success(savings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
