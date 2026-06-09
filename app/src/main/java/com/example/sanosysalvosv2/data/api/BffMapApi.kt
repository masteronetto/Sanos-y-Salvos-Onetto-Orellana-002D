package com.example.sanosysalvosv2.data.api

// INACTIVE: BFF map routing removed from MapsRepository.
// Retained for future emulator/backend integration testing.
// Re-wire in MapsRepository when testing with local Docker stack.

import com.example.sanosysalvosv2.model.NearbyReportsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BffMapApi {
    @GET("/api/v1/bff/map/reports/nearby")
    suspend fun nearbyReports(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusMeters") radiusMeters: Int = 3000,
    ): Response<NearbyReportsResponse>
}