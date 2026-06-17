package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.AdminCreateUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminUsersApi {
    @GET("/api:sanos-y-salvos-users/list")
    suspend fun getUsers(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 1000,
    ): Response<Any>

    @GET("/api:sanos-y-salvos-users/get_by_id/{id}")
    suspend fun getUserById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Any>

    @POST("/api:sanos-y-salvos-users/create")
    suspend fun createUser(
        @Header("Authorization") authHeader: String,
        @Body request: AdminCreateUserRequest,
    ): Response<Any>

    @PUT("/api:sanos-y-salvos-users/update/{id}")
    suspend fun updateUser(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body request: Map<String, String>,
    ): Response<Any>

    @DELETE("/api:sanos-y-salvos-users/delete/{id}")
    suspend fun deleteUser(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Any>
}
