package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.AdminCoincidenciasApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminCoincidenciaDetailResponse
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummary
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummaryResponse
import com.google.gson.JsonElement
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class AdminCoincidenciasRepository {
    private val tag = "AdminCoincidenciasRepo"

    private fun api(): AdminCoincidenciasApi = XanoRetrofitClient.retrofit.create(AdminCoincidenciasApi::class.java)

    suspend fun listMatches(token: String): MapsResult<List<AdminCoincidenciaSummary>> {
        val requestUrl = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-matches/list"
        Log.d(tag, "GET $requestUrl")

        return try {
            val response = api().listMatches(authHeader = "Bearer $token")
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                val matches = parseMatchesJson(body)
                MapsResult.Success(matches)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al listar coincidencias: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al listar coincidencias: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al listar coincidencias")
        }
    }

    private fun parseMatchesJson(body: JsonElement?): List<AdminCoincidenciaSummary> {
        if (body == null || body.isJsonNull) return emptyList()
        val gson = com.google.gson.Gson()
        return try {
            if (body.isJsonArray) {
                val list: List<AdminCoincidenciaSummaryResponse> = gson.fromJson(body, object : com.google.gson.reflect.TypeToken<List<AdminCoincidenciaSummaryResponse>>() {}.type)
                list.map { it.toSummary() }
            } else if (body.isJsonObject) {
                val obj = body.asJsonObject
                val candidateKeys = listOf("data", "results", "items", "matches", "list")
                for (key in candidateKeys) {
                    if (obj.has(key)) {
                        val el = obj.get(key)
                        if (el.isJsonArray) {
                            val list: List<AdminCoincidenciaSummaryResponse> = gson.fromJson(el, object : com.google.gson.reflect.TypeToken<List<AdminCoincidenciaSummaryResponse>>() {}.type)
                            return list.map { it.toSummary() }
                        }
                    }
                }
                // Fallback: maybe the object itself is a single summary object or a list-typed object
                if (obj.has("id")) {
                    val single = gson.fromJson(body, AdminCoincidenciaSummaryResponse::class.java)
                    return listOf(single.toSummary())
                }
                emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse matches JSON", e)
            emptyList()
        }
    }

    private fun AdminCoincidenciaSummaryResponse.toSummary() = AdminCoincidenciaSummary(
        id = id,
        sourceName = sourcePetName.orEmpty(),
        matchedName = matchedPetName.orEmpty(),
        score = score ?: 0,
        status = status.orEmpty(),
        comuna = comuna.orEmpty(),
        date = createdAt.orEmpty(),
    )

    suspend fun getMatchDetails(token: String, id: String): MapsResult<AdminCoincidenciaDetailResponse> {
        val requestUrl = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-matches/details/$id"
        Log.d(tag, "GET $requestUrl")

        return try {
            val response = api().getMatchDetails(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) MapsResult.Error("Detalles de coincidencia vacíos") else MapsResult.Success(body)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al obtener detalles de coincidencia: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al obtener detalles de coincidencia: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al obtener detalles de coincidencia")
        }
    }

    suspend fun confirmMatch(token: String, id: String): MapsResult<AdminCoincidenciaDetailResponse> {
        return try {
            val response = api().confirmMatch(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) MapsResult.Error("Respuesta vacía") else MapsResult.Success(body)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al confirmar coincidencia: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al confirmar coincidencia: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al confirmar coincidencia")
        }
    }

    suspend fun discardMatch(token: String, id: String): MapsResult<AdminCoincidenciaDetailResponse> {
        return try {
            val response = api().discardMatch(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) MapsResult.Error("Respuesta vacía") else MapsResult.Success(body)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al descartar coincidencia: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al descartar coincidencia: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al descartar coincidencia")
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
