package com.example.sanosysalvosv2.data.repository

// AUTH ROUTE: always Xano direct. BFF not used for auth.
// Reason: BFF is local Docker only — unreachable from physical devices.

import com.example.sanosysalvosv2.data.api.XanoAuthApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AuthResponse
import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest
import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class AuthRepository {
    private fun xanoApi(): XanoAuthApi = XanoRetrofitClient.retrofit.create(XanoAuthApi::class.java)

    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = callWithRecovery { it.register(request) }
        if (!response.isSuccessful) {
            throwAuthFailure("Registro", response)
        }

        return mapToAuthResponse(response.body() ?: throw Exception("Respuesta vacia del servidor"))
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = callWithRecovery { it.login(request) }
        if (!response.isSuccessful) {
            throwAuthFailure("Login", response)
        }

        return mapToAuthResponse(response.body() ?: throw Exception("Respuesta vacia del servidor"))
    }

    private suspend fun callWithRecovery(
        call: suspend (XanoAuthApi) -> Response<Map<String, Any?>>,
    ): Response<Map<String, Any?>> {
        val maxAttempts = 3
        repeat(maxAttempts) { attempt ->
            try {
                return call(xanoApi())
            } catch (e: Exception) {
                val retryable = e is IOException || e is SocketTimeoutException
                val isLastAttempt = attempt == maxAttempts - 1
                if (!retryable || isLastAttempt) throw e
                delay((attempt + 1) * 300L)
            }
        }

        throw IllegalStateException("No se pudo completar la solicitud de autenticacion")
    }

    private fun throwAuthFailure(action: String, response: Response<Map<String, Any?>>): Nothing {
        val code = response.code()
        val errorBody = response.errorBody()?.string().orEmpty()
        val normalized = errorBody.lowercase()

        if (code == 401 || code == 403) {
            if (normalized.contains("invalid credentials") || normalized.contains("credencial")) {
                throw IllegalArgumentException("Correo o contrasena incorrectos")
            }
            throw IllegalStateException("Sesion no autorizada")
        }

        if (code == 409) {
            throw IllegalArgumentException("El correo ya esta registrado")
        }

        throw HttpException(response)
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
