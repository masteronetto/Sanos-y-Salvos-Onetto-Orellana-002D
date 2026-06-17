package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.UserReportsApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.model.ReportResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response
import com.example.sanosysalvosv2.model.PaginatedResponse

class UserReportsRepository {
    private val tag = "UserReportsRepository"

    private fun api(): UserReportsApi = XanoRetrofitClient.retrofit.create(UserReportsApi::class.java)

    suspend fun listMyReports(token: String): MapsResult<List<ReportResponse>> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/my_reports"
        Log.d(tag, "GET $url")
        return try {
            val response = api().listMyReports(authHeader = "Bearer $token")
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body: JsonElement? = response.body()
                if (body == null) {
                    MapsResult.Error("Respuesta vacía")
                } else {
                    val list = parseReportsJson(body)
                    MapsResult.Success(list)
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red: ${e.message}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    suspend fun getReportDetails(token: String, id: String): MapsResult<ReportResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/details/$id"
        Log.d(tag, "GET $url")
        return safeCall { api().getReportDetails(authHeader = "Bearer $token", id = id) }
    }

    suspend fun createReport(token: String, request: ReportRequest): MapsResult<ReportResponse> {
        val debugJson = Gson().toJson(request)
        Log.d(tag, "DEBUG_JSON Exact JSON sent to API: $debugJson")

        return try {
            val response = api().createReport(authHeader = "Bearer $token", request = request)
            val responseCode = response.code()
            Log.d(tag, "DEBUG_JSON Response code: $responseCode")
            val errorBody = if (!response.isSuccessful) response.errorBody()?.string() else null

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(tag, "Create Report Success: code=$responseCode body=${Gson().toJson(responseBody)}")
                if (responseBody == null) {
                    MapsResult.Error("Respuesta de reporte vacía")
                } else {
                    MapsResult.Success(responseBody)
                }
            } else {
                Log.e(tag, "DEBUG_JSON Error body: ${errorBody ?: "<empty>"}")
                Log.d(tag, "Create Report Error: code=$responseCode error=${errorBody ?: "<empty>"}")
                MapsResult.Error("Reports API fallido: $responseCode - ${errorBody ?: "sin detalle"}")
            }
        } catch (e: IOException) {
            Log.e(tag, "Network error creating report", e)
            MapsResult.Error("Error de red en reportes: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            Log.e(tag, "HTTP exception creating report", e)
            MapsResult.Error("Error HTTP en reportes: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            Log.e(tag, "Error creating report", e)
            MapsResult.Error(e.message ?: "Error inesperado en reportes")
        }
    }

    suspend fun updateReport(token: String, id: String, request: ReportRequest): MapsResult<ReportResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/update/$id"
        Log.d(tag, "PUT $url")
        return safeCall { api().updateReport(authHeader = "Bearer $token", id = id, request = request.toMap()) }
    }

    suspend fun updateReportFields(token: String, id: String, fields: Map<String, Any?>): MapsResult<ReportResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/update/$id"
        Log.d(tag, "PUT $url with fields: ${fields.keys}")
        return safeCall { api().updateReport(authHeader = "Bearer $token", id = id, request = fields) }
    }

    suspend fun getAllReports(token: String, type: String? = null, page: Int = 1): MapsResult<List<ReportResponse>> {
        return try {
            val response = api().listReports(authHeader = "Bearer $token", page = page, perPage = 20, type = type)
            if (response.isSuccessful) {
                MapsResult.Success(response.body()?.items ?: emptyList())
            } else if (response.code() == 401) {
                MapsResult.Error("Sesión expirada")
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red: ${e.message}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    suspend fun searchNearbyReports(token: String, lat: Double, lng: Double, radiusMeters: Int = 5000, type: String? = null): MapsResult<List<ReportResponse>> {
        return try {
            val response = api().searchNearby(authHeader = "Bearer $token", lat = lat, lng = lng, radiusMeters = radiusMeters, type = type)
            if (response.isSuccessful) {
                MapsResult.Success(response.body()?.items ?: emptyList())
            } else if (response.code() == 401) {
                MapsResult.Error("Sesión expirada")
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red: ${e.message}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    suspend fun markAsResolved(token: String, id: String): MapsResult<ReportResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/update/$id"
        Log.d(tag, "PUT $url")
        return safeCall { api().updateReport(authHeader = "Bearer $token", id = id, request = mapOf("status" to "RESOLVED")) }
    }

    suspend fun deleteReport(token: String, id: String): MapsResult<Unit> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/delete/$id"
        Log.d(tag, "DELETE $url")
        return try {
            val response = api().deleteReport(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                MapsResult.Success(Unit)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al eliminar reporte: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al eliminar reporte: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al eliminar reporte")
        }
    }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): MapsResult<T> {
        return try {
            val response = call()
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) {
                    MapsResult.Error("Respuesta de reporte vacía")
                } else {
                    MapsResult.Success(body)
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red en reportes: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP en reportes: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado en reportes")
        }
    }

    private fun parseHttpError(response: Response<*>): String {
        val backendError = try {
            response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val detail = backendError ?: response.message().ifBlank { "sin detalle" }
        return "Reports API fallido: ${response.code()} - $detail"
    }

    private fun parseReportsJson(body: JsonElement?): List<ReportResponse> {
        if (body == null || body.isJsonNull) return emptyList()
        val gson = Gson()
        return try {
            if (body.isJsonArray) {
                gson.fromJson(body, object : TypeToken<List<ReportResponse>>() {}.type)
            } else if (body.isJsonObject) {
                val obj = body.asJsonObject
                val candidateKeys = listOf("data", "results", "items", "reports", "list")
                for (key in candidateKeys) {
                    if (obj.has(key)) {
                        val el = obj.get(key)
                        if (el.isJsonArray) {
                            return gson.fromJson(el, object : TypeToken<List<ReportResponse>>() {}.type)
                        }
                    }
                }

                val single = gson.fromJson(body, ReportResponse::class.java)
                listOf(single)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse reports JSON", e)
            emptyList()
        }
    }

    private fun ReportRequest.toMap(): Map<String, Any?> {
        val base = mutableMapOf<String, Any?>(
            "type" to type,
            "description" to description,
            "latitude" to latitude,
            "longitude" to longitude,
            "eventDate" to eventDate,
        )

        locationName?.takeIf { it.isNotBlank() }?.let { base["locationName"] = it }
        species?.takeIf { it.isNotBlank() }?.let { base["species"] = it }
        breed?.takeIf { it.isNotBlank() }?.let { base["breed"] = it }
        color?.takeIf { it.isNotBlank() }?.let { base["color"] = it }

        // photoBase64: strip data URI prefix if present, include only when non-blank
        photoBase64?.replaceFirst(Regex("^data:image/[^;]+;base64,"), "")
            ?.takeIf { it.isNotBlank() }
            ?.let { base["photoBase64"] = it }

        // petId, petName and size: include only when non-null and non-blank
        petId?.takeIf { it.isNotBlank() }?.let { base["petId"] = it }
        petName?.takeIf { it.isNotBlank() }?.let { base["petName"] = it }
        size?.takeIf { it.isNotBlank() }?.let { base["size"] = it }

        return base
    }
}
