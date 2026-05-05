package com.sanosysalvos.androidapp.data.repository

import com.sanosysalvos.androidapp.data.api.AuthApi
import com.sanosysalvos.androidapp.data.api.RetrofitClient
import com.sanosysalvos.androidapp.model.AuthResponse
import com.sanosysalvos.androidapp.model.LoginRequest
import com.sanosysalvos.androidapp.model.RegisterRequest

class AuthRepository {

    private val api: AuthApi = RetrofitClient.retrofit.create(AuthApi::class.java)

    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = api.register(request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Register failed: ${'$'}{response.code()} - ${'$'}{response.message()}")
        }
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = api.login(request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Login failed: ${'$'}{response.code()} - ${'$'}{response.message()}")
        }
    }
}
