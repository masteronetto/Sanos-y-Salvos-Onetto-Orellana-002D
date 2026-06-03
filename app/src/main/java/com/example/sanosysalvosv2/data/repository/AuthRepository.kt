package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AuthApi
import com.example.sanosysalvosv2.data.api.RetrofitClient
import com.example.sanosysalvosv2.data.config.NetworkConfig
import com.example.sanosysalvosv2.model.AuthResponse
import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest
import java.io.IOException

class AuthRepository {
    private fun api(): AuthApi = RetrofitClient.retrofit().create(AuthApi::class.java)

    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = callWithRecovery { it.register(request) }
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
        val response = callWithRecovery { it.login(request) }
        if (!response.isSuccessful) {
            throw Exception("Login fallido: ${response.code()} - ${response.message()}")
        }

        val envelope = response.body() ?: throw Exception("Respuesta vacia del servidor")
        if (!envelope.success) {
            throw Exception(envelope.message ?: "No se pudo iniciar sesion")
        }

        return envelope.data ?: throw Exception("No se recibieron datos de sesion")
    }

    private suspend fun callWithRecovery(call: suspend (AuthApi) -> retrofit2.Response<com.example.sanosysalvosv2.model.ApiEnvelope<AuthResponse>>): retrofit2.Response<com.example.sanosysalvosv2.model.ApiEnvelope<AuthResponse>> {
        return try {
            call(api())
        } catch (e: Exception) {
            if (!isConnectivityError(e)) throw e

            val recovered = NetworkConfig.recoverBackendHost()
            if (recovered == null) {
                throw Exception("No se pudo conectar al backend automaticamente. Verifica que los puertos 8080 y 8081 esten expuestos en tu PC y en la misma red.")
            }

            call(api())
        }
    }

    private fun isConnectivityError(e: Exception): Boolean {
        if (e is IOException) return true
        val message = e.message?.lowercase().orEmpty()
        return message.contains("failed to connect") ||
            message.contains("timeout") ||
            message.contains("unable to resolve host")
    }
}
