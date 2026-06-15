package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.ReportResponse
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminReportsApi {
    @GET("/api:sanos-y-salvos-reports/list")
    suspend fun listReports(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("comuna") comuna: String? = null,
    ): Response<JsonElement>

    @GET("/api:sanos-y-salvos-reports/details/{id}")
    suspend fun getReportDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<ReportResponse>

    @PUT("/api:sanos-y-salvos-reports/update/{id}")
    suspend fun updateReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body body: Map<String, Any?>,
    ): Response<ReportResponse>

    @DELETE("/api:sanos-y-salvos-reports/delete/{id}")
    suspend fun deleteReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Map<String, Any?>>
}
