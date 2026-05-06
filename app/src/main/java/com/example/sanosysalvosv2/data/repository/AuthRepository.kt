package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AuthApi
import com.example.sanosysalvosv2.data.api.RetrofitClient
import com.example.sanosysalvosv2.model.AuthResponse
import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest

class AuthRepository {

    private val api: AuthApi = RetrofitClient.retrofit.create(AuthApi::class.java)

    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = api.register(request)
        if (!response.isSuccessful) {
            throw Exception("Registro fallido: ${response.code()} - ${response.message()}")
        }

        val envelope = response.body() ?: throw Exception("Respuesta vacia del servidor")
        if (!envelope.success) {
            throw Exception(envelope.message ?: "No se pudo registrar")
        }

        return envelope.data ?: throw Exception("No se recibieron datos de sesion")
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = api.login(request)
        if (!response.isSuccessful) {
            throw Exception("Login fallido: ${response.code()} - ${response.message()}")
        }

        val envelope = response.body() ?: throw Exception("Respuesta vacia del servidor")
        if (!envelope.success) {
            throw Exception(envelope.message ?: "No se pudo iniciar sesion")
        }

        return envelope.data ?: throw Exception("No se recibieron datos de sesion")
    }
}
