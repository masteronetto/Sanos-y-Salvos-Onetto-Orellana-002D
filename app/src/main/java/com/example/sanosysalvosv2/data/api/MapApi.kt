package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.NearbyReportsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MapApi {
    // Direct Xano endpoint (release path).
    @GET("/api:maps/reports/nearby")
    suspend fun nearbyReports(
        @Header("Authorization") authHeader: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusMeters") radiusMeters: Int = 3000,
    ): Response<NearbyReportsResponse>
}
