package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.ApiEnvelope
import com.example.sanosysalvosv2.model.AuthResponse
import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BffAuthApi {
    @POST("/api:sanos-y-salvos-auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, Any?>>

    @POST("/api:sanos-y-salvos-auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Map<String, Any?>>
}