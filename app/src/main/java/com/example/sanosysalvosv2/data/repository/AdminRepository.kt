package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AdminApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminUserSummary

class AdminRepository {
    private val api: AdminApi = XanoRetrofitClient.retrofit.create(AdminApi::class.java)

    suspend fun listRegisteredUsers(token: String): List<AdminUserSummary> {
        val response = api.listUsers(authHeader = "Bearer $token")
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
}
