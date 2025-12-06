package com.vlaados.freeze.data.repository

import com.vlaados.freeze.data.model.User
import com.vlaados.freeze.data.remote.ApiService
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getMe(token: String): Result<User> {
        return try {
            val user = apiService.getMe("Bearer $token")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(token: String, userId: Int, user: User): Result<User> {
        return try {
            val updatedUser = apiService.updateUser(userId, "Bearer $token", user)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addSavingsToGoal(token: String, amount: Double): Result<User> {
        return try {
            val user = apiService.getMe("Bearer $token")
            val currentSaved = user.saved_for_goal ?: 0.0
            val newSaved = currentSaved + amount
            val updatedUser = user.copy(saved_for_goal = newSaved)
            
            val userId = user.id ?: throw IllegalStateException("User ID is null")
            
            val result = apiService.updateUser(userId, "Bearer $token", updatedUser)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getAchievements(token: String): Result<List<com.vlaados.freeze.data.model.Achievement>> {
        return try {
            val achievements = apiService.getAchievements("Bearer $token")
            Result.success(achievements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyAchievements(token: String): Result<List<com.vlaados.freeze.data.model.UserAchievement>> {
        return try {
            val userAchievements = apiService.getMyAchievements("Bearer $token")
            Result.success(userAchievements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
