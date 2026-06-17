package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.data.api.UserCoincidenciasApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.UserMatchResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class UserCoincidenciasRepository {
    private val tag = "UserMatchesRepo"

    private fun api(): UserCoincidenciasApi = XanoRetrofitClient.retrofit.create(UserCoincidenciasApi::class.java)

    suspend fun listMyMatches(token: String, page: Int = 1, perPage: Int = 20): MapsResult<List<UserMatchResponse>> {
        return try {
            val response = api().getMyMatches(authHeader = "Bearer $token", page = page, perPage = perPage)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                MapsResult.Success(parseMatchesJson(body))
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al listar coincidencias: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al listar coincidencias: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            Log.e(tag, "listMyMatches error", e)
            MapsResult.Error(e.message ?: "Error inesperado al listar coincidencias")
        }
    }

    suspend fun acceptMatch(token: String, id: String): MapsResult<Unit> {
        return try {
            val response = api().acceptMatch(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                MapsResult.Success(Unit)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al aceptar coincidencia: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al aceptar coincidencia: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            Log.e(tag, "acceptMatch error", e)
            MapsResult.Error(e.message ?: "Error inesperado al aceptar coincidencia")
        }
    }

    suspend fun rejectMatch(token: String, id: String): MapsResult<Unit> {
        return try {
            val response = api().rejectMatch(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                MapsResult.Success(Unit)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al rechazar coincidencia: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al rechazar coincidencia: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            Log.e(tag, "rejectMatch error", e)
            MapsResult.Error(e.message ?: "Error inesperado al rechazar coincidencia")
        }
    }

    private fun parseMatchesJson(body: JsonElement?): List<UserMatchResponse> {
        if (body == null || body.isJsonNull) return emptyList()
        val gson = Gson()
        return try {
            if (body.isJsonArray) {
                gson.fromJson(body, object : TypeToken<List<UserMatchResponse>>() {}.type)
            } else {
                val obj = body.asJsonObject
                val candidateKeys = listOf("items", "data", "results", "list")
                for (key in candidateKeys) {
                    if (obj.has(key) && obj.get(key).isJsonArray) {
                        return gson.fromJson(obj.get(key), object : TypeToken<List<UserMatchResponse>>() {}.type)
                    }
                }
                if (obj.has("id")) {
                    listOf(gson.fromJson(body, UserMatchResponse::class.java))
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse coincidencias JSON", e)
            emptyList()
        }
    }

    private fun parseHttpError(response: Response<*>): String {
        val backendError = try {
            response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val detail = backendError ?: response.message().ifBlank { "sin detalle" }
        return "Coincidencias API fallido: ${response.code()} - $detail"
    }
}
