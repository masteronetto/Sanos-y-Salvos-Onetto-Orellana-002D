package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.BffAuthApi
import com.example.sanosysalvosv2.data.api.BffRetrofitClient
import com.example.sanosysalvosv2.model.AuthResponse
import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest

class AuthRepository {
    private fun bffApi(): BffAuthApi = BffRetrofitClient.retrofit().create(BffAuthApi::class.java)

    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = callWithRecovery { it.register(request) }
        if (!response.isSuccessful) {
            throw Exception("Registro fallido: ${response.code()} - ${response.message()}")
        }

        return mapToAuthResponse(response.body() ?: throw Exception("Respuesta vacia del servidor"))
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = callWithRecovery { it.login(request) }
        if (!response.isSuccessful) {
            throw Exception("Login fallido: ${response.code()} - ${response.message()}")
        }

        return mapToAuthResponse(response.body() ?: throw Exception("Respuesta vacia del servidor"))
    }

    private suspend fun callWithRecovery(
        call: suspend (BffAuthApi) -> retrofit2.Response<Map<String, Any?>>,
    ): retrofit2.Response<Map<String, Any?>> {
        return call(bffApi())
    }

    private fun mapToAuthResponse(raw: Map<String, Any?>): AuthResponse {
        val data = raw["data"] as? Map<*, *>

        val token = (data?.get("token") as? String)
            ?: raw["token"] as? String
            ?: raw["auth_token"] as? String
            ?: throw Exception("No se recibio token de sesion")

        val userId = (data?.get("uid") as? String)
            ?: (data?.get("id") as? String)
            ?: (data?.get("userId") as? String)
            ?: raw["uid"] as? String
            ?: raw["id"] as? String
            ?: raw["userId"] as? String
            ?: raw["user_id"] as? String
            ?: ""

        val role = ((data?.get("role") as? String) ?: (raw["role"] as? String) ?: "USER").uppercase()

        return AuthResponse(
            userId = userId,
            role = role,
            token = token,
        )
    }
}
