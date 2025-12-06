package com.vlaados.freeze.data.remote

import com.vlaados.freeze.data.model.LoginResponse
import com.vlaados.freeze.data.model.User
import com.vlaados.freeze.data.model.Saving
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("users/")
    suspend fun register(@Body user: User): User

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): User

    @GET("savings/")
    suspend fun getSavings(@Header("Authorization") token: String): List<Saving>

    @PUT("users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Header("Authorization") token: String,
        @Body user: User
    ): User

    @POST("ask_ai/")
    suspend fun askAi(@Body request: AiRequest): AiResponse

    @POST("savings/")
    suspend fun addSaving(
        @Header("Authorization") token: String,
        @Body saving: Saving
    ): Saving

    @POST("freezes/")
    suspend fun addFreeze(
        @Header("Authorization") token: String,
        @Body freezeItem: com.vlaados.freeze.data.model.FreezeItem
    ): com.vlaados.freeze.data.model.FreezeItem

    @GET("freezes/")
    suspend fun getFreezes(
        @Header("Authorization") token: String
    ): List<com.vlaados.freeze.data.model.FreezeItem>

    @retrofit2.http.DELETE("freezes/{id}")
    suspend fun deleteFreeze(
        @Header("Authorization") token: String,
        @Path("id") freezeId: Int
    ): retrofit2.Response<Unit>

    @GET("groups/my")
    suspend fun getMyGroups(
        @Header("Authorization") token: String
    ): retrofit2.Response<List<com.vlaados.freeze.data.model.Group>>

    @GET("groups/{id}")
    suspend fun getGroupById(
        @Header("Authorization") token: String,
        @Path("id") groupId: Int
    ): retrofit2.Response<com.vlaados.freeze.data.model.Group>

    @POST("groups/")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: com.vlaados.freeze.data.model.CreateGroupRequest
    ): retrofit2.Response<com.vlaados.freeze.data.model.Group>

    @POST("groups/{group_id}/members/{user_id}")
    suspend fun addMemberToGroup(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int,
        @Path("user_id") userId: Int
    ): retrofit2.Response<Unit>

    @GET("users/{id}")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): retrofit2.Response<User>

    @PUT("groups/{group_id}")
    suspend fun updateGroup(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int,
        @Body request: com.vlaados.freeze.data.model.CreateGroupRequest
    ): retrofit2.Response<com.vlaados.freeze.data.model.Group>

    @POST("groups/{group_id}/save")
    suspend fun saveForGroupGoal(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int,
        @retrofit2.http.Query("amount") amount: Double
    ): retrofit2.Response<Unit>

    @GET("groups/{group_id}/members")
    suspend fun getGroupMembers(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int
    ): retrofit2.Response<List<com.vlaados.freeze.data.model.GroupMember>>

    @POST("groups/{group_id}/challenges/")
    suspend fun createChallenge(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int,
        @Body request: com.vlaados.freeze.data.model.CreateChallengeRequest
    ): retrofit2.Response<com.vlaados.freeze.data.model.ChallengeData>

    @GET("groups/{group_id}/challenges")
    suspend fun getGroupChallenges(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int
    ): retrofit2.Response<List<com.vlaados.freeze.data.model.ChallengeData>>
    @POST("ssilkai/")
    suspend fun askSsilkai(@Body request: LinkRequest): Any

    @GET("achievements/")
    suspend fun getAchievements(
        @Header("Authorization") token: String
    ): List<com.vlaados.freeze.data.model.Achievement>

    @GET("users/me/achievements")
    suspend fun getMyAchievements(
        @Header("Authorization") token: String
    ): List<com.vlaados.freeze.data.model.UserAchievement>

    @PUT("groups/{group_id}/members/{user_id}")
    suspend fun updateGroupMember(
        @Header("Authorization") token: String,
        @Path("group_id") groupId: Int,
        @Path("user_id") userId: Int,
        @Body request: UpdateMemberRequest
    ): retrofit2.Response<Any>

    @POST("ask/")
    suspend fun ask(
        @Body request: AskRequest
    ): retrofit2.Response<AskResponse>
}

data class AiRequest(
    val prompt: String,
    val system_prompt: String? = "You are a helpful assistant"
)

data class AiResponse(
    val output: Any?
)

data class LinkRequest(
    val prompt: String,
    val reasoning_effort: String = "high"
)

data class UpdateMemberRequest(
    val saved_amount: Double
)

data class AskRequest(
    val message: String
)

data class AskResponse(
    val response: String
)
