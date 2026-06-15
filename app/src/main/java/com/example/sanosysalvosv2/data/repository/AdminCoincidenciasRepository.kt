package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.AdminCoincidenciasApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminCoincidenciaDetailResponse
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummary
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
                if (body == null) {
                    MapsResult.Error("Lista de coincidencias vacía")
                } else {
                    val matches = body.map { item ->
                        AdminCoincidenciaSummary(
                            id = item.id,
                            sourceName = item.sourcePetName.orEmpty(),
                            matchedName = item.matchedPetName.orEmpty(),
                            score = item.score ?: 0,
                            status = item.status.orEmpty(),
                            comuna = item.comuna.orEmpty(),
                            date = item.createdAt.orEmpty(),
                        )
                    }
                    MapsResult.Success(matches)
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al listar coincidencias: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al listar coincidencias: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al listar coincidencias")
        }
    }

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
