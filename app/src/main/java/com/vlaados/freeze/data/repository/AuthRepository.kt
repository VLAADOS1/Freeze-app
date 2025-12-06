package com.vlaados.freeze.data.repository

import com.vlaados.freeze.data.model.LoginResponse
import com.vlaados.freeze.data.model.User
import com.vlaados.freeze.data.remote.ApiService
import javax.inject.Inject

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun register(username: String, password: String): Result<User>
}

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(username, password)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(username: String, password: String): Result<User> {
        return try {
            val user = User(username = username, password = password, role = "user")
            val response = apiService.register(user)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
