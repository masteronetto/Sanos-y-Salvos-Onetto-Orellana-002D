package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AdminApi
import com.example.sanosysalvosv2.data.api.RetrofitClient
import com.example.sanosysalvosv2.data.config.NetworkConfig
import com.example.sanosysalvosv2.model.AdminUserSummary
import java.io.IOException

class AdminRepository {
    private fun api(): AdminApi = RetrofitClient.retrofit().create(AdminApi::class.java)

    suspend fun listRegisteredUsers(token: String): List<AdminUserSummary> {
        val response = try {
            api().listUsers(authHeader = "Bearer $token")
        } catch (e: Exception) {
            if (!isConnectivityError(e)) throw e
            val recovered = NetworkConfig.recoverBackendHost()
            if (recovered == null) {
                throw Exception("No se pudo conectar al backend automaticamente. Verifica puertos 8080/8081 y red local.")
            }
            api().listUsers(authHeader = "Bearer $token")
        }
        if (!response.isSuccessful) {
            throw Exception("No se pudo listar usuarios: ${response.code()} - ${response.message()}")
        }

        val body = response.body() ?: emptyList()
        return body.map { mapUser(it) }
    }

    private fun mapUser(raw: Map<String, Any?>): AdminUserSummary {
        val id = raw["id"]?.toString() ?: raw["uid"]?.toString() ?: ""
        val fullName = raw["fullName"]?.toString() ?: raw["name"]?.toString() ?: "Sin nombre"
        val email = raw["email"]?.toString() ?: "Sin email"
        val role = raw["role"]?.toString()?.uppercase() ?: "USER"

        return AdminUserSummary(
            id = id,
            fullName = fullName,
            email = email,
            role = role,
        )
    }

    private fun isConnectivityError(e: Exception): Boolean {
        if (e is IOException) return true
        val message = e.message?.lowercase().orEmpty()
        return message.contains("failed to connect") ||
            message.contains("timeout") ||
            message.contains("unable to resolve host")
    }
}
