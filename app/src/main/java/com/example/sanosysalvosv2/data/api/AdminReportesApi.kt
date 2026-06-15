package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.AdminReportDetailResponse
import com.example.sanosysalvosv2.model.AdminReportSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminReportesApi {
    @GET("/api:sanos-y-salvos-mascotas/reports")
    suspend fun listReports(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("comuna") comuna: String? = null,
    ): Response<List<AdminReportSummaryResponse>>

    @GET("/api:sanos-y-salvos-mascotas/reports/{id}")
    suspend fun getReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<AdminReportDetailResponse>

    @PATCH("/api:sanos-y-salvos-mascotas/reports/{id}/approve")
    suspend fun approveReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body body: Map<String, String>,
    ): Response<AdminReportDetailResponse>

    @PATCH("/api:sanos-y-salvos-mascotas/reports/{id}/reject")
    suspend fun rejectReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body body: Map<String, String>,
    ): Response<AdminReportDetailResponse>
}
