package com.example.sanosysalvosv2.data.api

// Direct Xano pet report API. Always reachable from emulator and physical devices.
// Path: POST /api:sanos-y-salvos-mascotas/report
// Adjust the Xano API group name here if the Xano workspace uses a different group.

import com.example.sanosysalvosv2.model.PetReportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PetApi {
    @POST("/api:sanos-y-salvos-mascotas/report")
    suspend fun createReport(
        @Header("Authorization") authHeader: String,
        @Body request: PetReportRequest,
    ): Response<Map<String, Any?>>
}
