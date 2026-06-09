package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.MapApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.NearbyReport
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

sealed interface MapsResult<out T> {
    data class Success<T>(val data: T) : MapsResult<T>
    data class Error(val message: String) : MapsResult<Nothing>
}

class MapsRepository {
    private val tag = "MapsRepository"

    // MAP ROUTE: Xano direct for all builds and devices.
    // BFF map path (BffMapApi) removed from active routing — unreachable from
    // physical devices. Re-enable only when running on emulator with Docker up.
    private fun xanoApi(): MapApi = XanoRetrofitClient.retrofit.create(MapApi::class.java)

    suspend fun getNearbyReports(lat: Double, lon: Double, radiusMeters: Int = 3000): MapsResult<List<NearbyReport>> {
        val requestUrl = "${BuildConfig.XANO_BASE_URL}api:maps/reports/nearby?lat=$lat&lng=$lon&radiusMeters=$radiusMeters"
        Log.d(tag, "GET $requestUrl")

        return try {
            val response = xanoApi().nearbyReports(lat = lat, lng = lon, radiusMeters = radiusMeters)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) {
                    MapsResult.Error("Nearby reports sin contenido")
                } else {
                    val reports = body.markers.map { marker ->
                        NearbyReport(
                            lat = marker.latitude,
                            lon = marker.longitude,
                            title = marker.title,
                            description = "${marker.reportType.name.lowercase()} · ${marker.distanceMeters.toInt()} m",
                        )
                    }
                    MapsResult.Success(reports)
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al consultar reportes cercanos: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al consultar reportes cercanos: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al consultar reportes cercanos")
        }
    }

    private fun parseHttpError(response: Response<*>): String {
        val backendError = try {
            response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val detail = backendError ?: response.message().ifBlank { "sin detalle" }
        return "Nearby reports fallido: ${response.code()} - $detail"
    }
}