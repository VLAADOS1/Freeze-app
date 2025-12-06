package com.vlaados.freeze.data.repository

import com.vlaados.freeze.data.model.User

interface UserRepository {
    suspend fun getMe(token: String): Result<User>
    suspend fun updateUser(token: String, userId: Int, user: User): Result<User>
    suspend fun addSavingsToGoal(token: String, amount: Double): Result<User>
    suspend fun getAchievements(token: String): Result<List<com.vlaados.freeze.data.model.Achievement>>
    suspend fun getMyAchievements(token: String): Result<List<com.vlaados.freeze.data.model.UserAchievement>>
}
