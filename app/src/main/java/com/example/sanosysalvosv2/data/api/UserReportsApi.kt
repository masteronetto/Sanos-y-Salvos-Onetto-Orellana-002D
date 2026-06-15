package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.model.ReportResponse
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.sanosysalvosv2.model.PaginatedResponse

interface UserReportsApi {
    @GET("/api:sanos-y-salvos-reports/my_reports")
    suspend fun listMyReports(
        @Header("Authorization") authHeader: String,
    ): Response<JsonElement>

    @GET("/api:sanos-y-salvos-reports/details/{id}")
    suspend fun getReportDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<ReportResponse>

    @POST("/api:sanos-y-salvos-reports/create")
    suspend fun createReport(
        @Header("Authorization") authHeader: String,
        @Body request: ReportRequest,
    ): Response<ReportResponse>

    @PUT("/api:sanos-y-salvos-reports/update/{id}")
    suspend fun updateReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ReportResponse>

    @DELETE("/api:sanos-y-salvos-reports/delete/{id}")
    suspend fun deleteReport(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Map<String, Any?>>

    @GET("/api:sanos-y-salvos-reports/list")
    suspend fun listReports(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("type") type: String? = null,
    ): Response<PaginatedResponse<ReportResponse>>

    @GET("/api:sanos-y-salvos-reports/search")
    suspend fun searchNearby(
        @Header("Authorization") authHeader: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_meters") radiusMeters: Int = 5000,
        @Query("type") type: String? = null,
    ): Response<PaginatedResponse<ReportResponse>>
}
