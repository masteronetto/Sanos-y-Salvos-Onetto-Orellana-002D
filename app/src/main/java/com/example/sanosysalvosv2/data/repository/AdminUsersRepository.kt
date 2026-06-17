package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.AdminUsersApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminCreateUserRequest
import com.example.sanosysalvosv2.model.AdminUserSummary
import com.example.sanosysalvosv2.model.XanoUserResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Response

class AdminUsersRepository {
    private fun api(): AdminUsersApi = XanoRetrofitClient.retrofit.create(AdminUsersApi::class.java)

    suspend fun listUsers(token: String, page: Int = 1, perPage: Int = 1000): List<AdminUserSummary> {
        val response = api().getUsers(authHeader = "Bearer $token", page = page, perPage = perPage)
        if (!response.isSuccessful) {
            val errorBody = try { response.errorBody()?.string() ?: "" } catch (e: Exception) { "" }
            throw Exception("No se pudo listar usuarios: ${response.code()} - $errorBody")
        }
        val body = response.body() ?: return emptyList()
        val gson = Gson()
        val rawList: List<XanoUserResponse> = when {
            body.isJsonArray -> gson.fromJson(body, object : TypeToken<List<XanoUserResponse>>() {}.type)
            body.isJsonObject -> {
                val obj = body.asJsonObject
                val key = listOf("items", "data", "results", "list").firstOrNull { obj.has(it) }
                if (key != null) gson.fromJson(obj.get(key), object : TypeToken<List<XanoUserResponse>>() {}.type)
                else emptyList()
            }
            else -> emptyList()
        }
        return rawList.map { u ->
            AdminUserSummary(
                id       = u.id?.toLong()?.toString() ?: "",
                fullName = u.fullName ?: u.name ?: "Sin nombre",
                email    = u.email ?: "",
                phone    = u.phone ?: u.phoneNumber ?: "",
                role     = u.role?.uppercase() ?: "USUARIO",
                status   = u.status?.uppercase() ?: "ACTIVO",
            )
        }
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
