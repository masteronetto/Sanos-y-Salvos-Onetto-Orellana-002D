package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AdminUsuariosApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminUserSummary

class AdminUsuariosRepository {
    private fun api(): AdminUsuariosApi = XanoRetrofitClient.retrofit.create(AdminUsuariosApi::class.java)

    suspend fun getUsers(token: String): List<AdminUserSummary> {
        val response = api().getUsers(authHeader = "Bearer $token")
        if (!response.isSuccessful) {
            throw Exception("No se pudo listar usuarios: ${response.code()} - ${response.message()}")
        }

        val body = unwrapList(response.body())
        return body.map { mapUser(it) }
    }

    suspend fun deleteUser(token: String, userId: String): Boolean {
        val response = api().deleteUser(authHeader = "Bearer $token", userId = userId)
        return response.isSuccessful
    }

    suspend fun updateUserStatus(token: String, userId: String, status: String): Boolean {
        val response = api().updateUserStatus(
            authHeader = "Bearer $token",
            userId = userId,
            statusBody = mapOf("status" to status),
        )
        return response.isSuccessful
    }

    @Suppress("UNCHECKED_CAST")
    private fun unwrapList(raw: Any?): List<Map<String, Any?>> {
        return when (raw) {
            is List<*> -> raw.filterIsInstance<Map<String, Any?>>()
            is Map<*, *> -> {
                val data = raw["data"]
                if (data is List<*>) data.filterIsInstance<Map<String, Any?>>() else emptyList()
            }
            else -> emptyList()
        }
    }

    private fun mapUser(raw: Map<String, Any?>): AdminUserSummary {
        val id = raw["id"]?.toString() ?: raw["uid"]?.toString() ?: ""
        val fullName = raw["fullName"]?.toString() ?: raw["name"]?.toString() ?: "Sin nombre"
        val email = raw["email"]?.toString() ?: "Sin email"
        val role = raw["role"]?.toString()?.uppercase() ?: "USER"
        val phone = raw["phone"]?.toString() ?: raw["phoneNumber"]?.toString() ?: ""
        val status = raw["status"]?.toString()?.uppercase() ?: ""

        return AdminUserSummary(
            id = id,
            fullName = fullName,
            email = email,
            role = role,
            phone = phone,
            status = status,
        )
    }
}
