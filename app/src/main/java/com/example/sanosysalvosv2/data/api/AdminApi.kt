package com.example.sanosysalvosv2.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AdminApi {
    @GET("api/v1/users/admin/list")
    suspend fun listUsers(
        @Header("Authorization") authHeader: String,
    ): Response<List<Map<String, Any?>>>
}
