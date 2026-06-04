package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.MapLayer
import com.example.sanosysalvosv2.model.NearbyReportsResponse
import com.example.sanosysalvosv2.model.TileProviderConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MapApi {
    @GET("/api:maps/provider")
    suspend fun provider(): Response<TileProviderConfig>

    @GET("/api:maps/layers")
    suspend fun layers(): Response<List<MapLayer>>

    @GET("/api:maps/reports/nearby")
    suspend fun nearbyReports(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusMeters") radiusMeters: Int = 3000,
    ): Response<NearbyReportsResponse>
}
