package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AdminUsersApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminCreateUserRequest
import com.example.sanosysalvosv2.model.AdminUserSummary
import retrofit2.Response

class AdminUsersRepository {
    private fun api(): AdminUsersApi = XanoRetrofitClient.retrofit.create(AdminUsersApi::class.java)

    suspend fun listUsers(token: String): List<AdminUserSummary> {
        val response = api().getUsers(authHeader = "Bearer $token")
        if (!response.isSuccessful) {
            throw Exception("No se pudo listar usuarios: ${response.code()} ${response.message()}")
        }

        return unwrapList(response.body()).map { mapUser(it) }
    }

    suspend fun getUserById(token: String, id: String): AdminUserSummary {
        val response = api().getUserById(authHeader = "Bearer $token", id = id)
        if (!response.isSuccessful) {
            throw Exception("No se pudo obtener usuario: ${response.code()} ${response.message()}")
        }

        val body = response.body() as? Map<String, Any?>
            ?: throw Exception("Usuario inválido")
        return mapUser(body)
    }

    suspend fun createUser(token: String, request: AdminCreateUserRequest): AdminUserSummary {
        val response = api().createUser(authHeader = "Bearer $token", request = request)
        if (!response.isSuccessful) {
            throw Exception("No se pudo crear usuario: ${response.code()} ${response.message()}")
        }

        val body = response.body() as? Map<String, Any?>
            ?: throw Exception("Respuesta de creación inválida")
        return mapUser(body)
    }

    suspend fun updateUser(token: String, id: String, fields: Map<String, String>): AdminUserSummary {
        val response = api().updateUser(authHeader = "Bearer $token", id = id, request = fields)
        if (!response.isSuccessful) {
            throw Exception("No se pudo actualizar usuario: ${response.code()} ${response.message()}")
        }

        val body = response.body() as? Map<String, Any?>
            ?: throw Exception("Respuesta de actualización inválida")
        return mapUser(body)
    }

    suspend fun deleteUser(token: String, id: String): Boolean {
        val response = api().deleteUser(authHeader = "Bearer $token", id = id)
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
        val email = raw["email"]?.toString() ?: ""
        val phone = raw["phone"]?.toString() ?: raw["phoneNumber"]?.toString() ?: ""
        val role = raw["role"]?.toString()?.uppercase() ?: "USUARIO"
        val status = raw["status"]?.toString()?.uppercase() ?: "ACTIVO"

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
