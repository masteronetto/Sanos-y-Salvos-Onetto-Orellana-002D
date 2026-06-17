package com.example.sanosysalvosv2.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminUsuariosApi {
    @GET("/api:sanos-y-salvos-users/list")
    suspend fun getUsers(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 1000,
    ): Response<Any>

    @DELETE("/api:sanos-y-salvos-users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") authHeader: String,
        @Path("id") userId: String,
    ): Response<Any>

    @PATCH("/api:sanos-y-salvos-users/{id}/status")
    suspend fun updateUserStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") userId: String,
        @Body statusBody: Map<String, String>,
    ): Response<Any>
}
