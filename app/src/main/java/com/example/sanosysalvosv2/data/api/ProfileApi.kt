package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.UpdateProfileRequest
import com.example.sanosysalvosv2.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfileApi {
    @GET("/api:sanos-y-salvos-auth/me")
    suspend fun getMe(
        @Header("Authorization") authHeader: String,
    ): Response<UserProfileResponse>

    @PUT("/api:sanos-y-salvos-users/update/{id}")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body body: UpdateProfileRequest,
    ): Response<UserProfileResponse>

    @POST("/api:sanos-y-salvos-auth/logout")
    suspend fun logout(
        @Header("Authorization") authHeader: String,
    ): Response<Map<String, Any?>>
}
