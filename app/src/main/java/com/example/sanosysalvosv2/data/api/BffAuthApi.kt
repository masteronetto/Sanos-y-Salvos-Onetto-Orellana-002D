package com.example.sanosysalvosv2.data.api

// INACTIVE: auth was moved to XanoAuthApi + XanoRetrofitClient.
// BFF auth gateway still exists on the backend for future use.
// Safe to delete this file if BFF auth is never reactivated.
// AUTH ROLE: active-client
// CALLS: /api/v1/bff/auth/login and /api/v1/bff/auth/register
// STATUS: aligned-with-bff
// NOTE: Declared paths match BFF AuthGatewayController auth routes.

import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BffAuthApi {
    @POST("/api/v1/bff/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, Any?>>

    @POST("/api/v1/bff/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Map<String, Any?>>
}