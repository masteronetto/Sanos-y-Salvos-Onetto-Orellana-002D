package com.sanosysalvos.androidapp.data.api

import com.sanosysalvos.androidapp.model.AuthResponse
import com.sanosysalvos.androidapp.model.LoginRequest
import com.sanosysalvos.androidapp.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
