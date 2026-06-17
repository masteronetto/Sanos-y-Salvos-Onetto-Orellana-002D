package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.AdminStatsResponse
import retrofit2.Response
import retrofit2.http.GET

interface AdminStatsApi {
    @GET("/api:admin-stats/dashboard")
    suspend fun getDashboard(): Response<AdminStatsResponse>

    @GET("/api:admin-stats/recovery")
    suspend fun getRecovery(): Response<AdminStatsResponse>

    @GET("/api:admin-stats/reports-by-commune")
    suspend fun getReportsByCommune(): Response<AdminStatsResponse>
}
