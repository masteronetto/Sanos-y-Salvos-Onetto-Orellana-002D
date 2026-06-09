package com.example.sanosysalvosv2.data.api

// Direct Xano admin list. Always reachable from emulator and physical devices.
// BFF admin gateway exists on backend but requires local Docker — not used here.

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AdminApi {
    @GET("/api:sanos-y-salvos-users/list")
    suspend fun listUsers(
        @Header("Authorization") authHeader: String,
    ): Response<Any>
}
