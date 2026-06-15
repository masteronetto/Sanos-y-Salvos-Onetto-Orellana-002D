package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.AdminReportsApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.ReportResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class AdminReportsRepository {
    private val tag = "AdminReportsRepository"

    private fun api(): AdminReportsApi = XanoRetrofitClient.retrofit.create(AdminReportsApi::class.java)

    suspend fun listReports(token: String, type: String?, status: String?, comuna: String?): MapsResult<List<ReportResponse>> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/list"
        Log.d(tag, "GET $url")
        return try {
            val response = api().listReports(authHeader = "Bearer $token", type = type, status = status, comuna = comuna)
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
            MapsResult.Error("Error de red al listar reportes: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al listar reportes: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al listar reportes")
        }
    }

    suspend fun getReportDetails(token: String, id: String): MapsResult<ReportResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/details/$id"
        Log.d(tag, "GET $url")
        return safeCall { api().getReportDetails(authHeader = "Bearer $token", id = id) }
    }

    suspend fun updateReport(token: String, id: String, body: Map<String, Any?>): MapsResult<ReportResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-reports/update/$id"
        Log.d(tag, "PUT $url")
        return safeCall { api().updateReport(authHeader = "Bearer $token", id = id, body = body) }
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
                    MapsResult.Error("Respuesta vacía")
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

                // Fallback: try parsing object as single ReportResponse
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
}
