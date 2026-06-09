package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.PetApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.PetReportRequest
import com.example.sanosysalvosv2.model.PetReportResponse
import java.io.IOException
import retrofit2.HttpException

sealed interface PetResult<out T> {
    data class Success<T>(val data: T) : PetResult<T>
    data class Error(val message: String) : PetResult<Nothing>
}

class PetRepository {
    // PET REPORT ROUTE: Xano direct. Always reachable from emulator and physical devices.
    private fun api(): PetApi = XanoRetrofitClient.retrofit.create(PetApi::class.java)

    suspend fun createReport(token: String, request: PetReportRequest): PetResult<PetReportResponse> {
        return try {
            val response = api().createReport(
                authHeader = "Bearer $token",
                request = request,
            )
            if (!response.isSuccessful) {
                PetResult.Error("Error al crear reporte: ${response.code()} - ${response.message()}")
            } else {
                val raw = response.body()
                    ?: return PetResult.Error("Respuesta vacía del servidor")
                // Handle both wrapped { "data": {...} } and flat response shapes
                val data = raw["data"] as? Map<*, *> ?: raw
                val id = data["id"]?.toString()
                    ?: raw["id"]?.toString()
                    ?: ""
                val status = data["status"]?.toString()
                    ?: raw["status"]?.toString()
                    ?: "created"
                PetResult.Success(PetReportResponse(id = id, status = status))
            }
        } catch (e: IOException) {
            PetResult.Error("Error de red: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            PetResult.Error("Error HTTP: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            PetResult.Error(e.message ?: "Error inesperado al crear el reporte")
        }
    }
}
