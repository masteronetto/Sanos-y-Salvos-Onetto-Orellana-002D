package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.AdminReportesApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminReportDetailResponse
import com.example.sanosysalvosv2.model.AdminReportSummary
import com.example.sanosysalvosv2.model.AdminReportSummaryResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class AdminReportesRepository {
    private val tag = "AdminReportesRepository"

    private fun api(): AdminReportesApi = XanoRetrofitClient.retrofit.create(AdminReportesApi::class.java)

    suspend fun listReports(token: String, type: String?, status: String?, comuna: String?): MapsResult<List<AdminReportSummary>> {
        val requestUrl = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-mascotas/reports?type=$type&status=$status&comuna=$comuna"
        Log.d(tag, "GET $requestUrl")

        return try {
            val response = api().listReports(authHeader = "Bearer $token", type = type, status = status, comuna = comuna)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) {
                    MapsResult.Error("Lista de reportes vacía")
                } else {
                    val reportResponses = parseReportsJson(body)
                    val summaries = reportResponses.map { r ->
                        AdminReportSummary(
                            id = r.id,
                            name = r.petName ?: "-",
                            reportedBy = r.reporterName ?: "-",
                            comuna = r.comuna ?: "-",
                            date = r.createdAt ?: "-",
                            caseType = r.type ?: "",
                            status = r.status ?: "",
                        )
                    }
                    MapsResult.Success(summaries)
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

    suspend fun getReport(token: String, id: String): MapsResult<AdminReportDetailResponse> {
        return try {
            val response = api().getReport(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) MapsResult.Error("Reporte sin contenido") else MapsResult.Success(body)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al obtener reporte: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al obtener reporte: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al obtener reporte")
        }
    }

    suspend fun approveReport(token: String, id: String): MapsResult<AdminReportDetailResponse> {
        return try {
            val response = api().approveReport(authHeader = "Bearer $token", id = id, body = mapOf("status" to "APPROVED"))
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) MapsResult.Error("Respuesta vacía") else MapsResult.Success(body)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al aprobar reporte: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al aprobar reporte: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al aprobar reporte")
        }
    }

    suspend fun rejectReport(token: String, id: String): MapsResult<AdminReportDetailResponse> {
        return try {
            val response = api().rejectReport(authHeader = "Bearer $token", id = id, body = mapOf("status" to "REJECTED"))
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) MapsResult.Error("Respuesta vacía") else MapsResult.Success(body)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al rechazar reporte: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al rechazar reporte: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al rechazar reporte")
        }
    }

    private fun parseHttpError(response: Response<*>): String {
        val backendError = try {
            response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val detail = backendError ?: response.message().ifBlank { "sin detalle" }
        return "Reportes API fallido: ${response.code()} - $detail"
    }

    private fun parseReportsJson(body: JsonElement?): List<AdminReportSummaryResponse> {
        if (body == null || body.isJsonNull) return emptyList()
        val gson = Gson()
        return try {
            if (body.isJsonArray) {
                gson.fromJson(body, object : TypeToken<List<AdminReportSummaryResponse>>() {}.type)
            } else if (body.isJsonObject) {
                val obj = body.asJsonObject
                val candidateKeys = listOf("data", "results", "items", "reports", "list")
                for (key in candidateKeys) {
                    if (obj.has(key)) {
                        val el = obj.get(key)
                        if (el.isJsonArray) {
                            return gson.fromJson(el, object : TypeToken<List<AdminReportSummaryResponse>>() {}.type)
                        }
                    }
                }
                gson.fromJson(body, object : TypeToken<List<AdminReportSummaryResponse>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse admin report summaries JSON", e)
            emptyList()
        }
    }
}
