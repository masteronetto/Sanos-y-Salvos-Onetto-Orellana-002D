package com.example.sanosysalvosv2.data.repository

import com.example.sanosysalvosv2.data.api.BffRetrofitClient
import com.example.sanosysalvosv2.data.api.MapApi
import com.example.sanosysalvosv2.model.MapLayer
import com.example.sanosysalvosv2.model.NearbyReportsResponse
import com.example.sanosysalvosv2.model.TileProviderConfig

class MapRepository {
    private val api: MapApi = BffRetrofitClient.retrofit.create(MapApi::class.java)

    suspend fun provider(): TileProviderConfig {
        val response = api.provider()
        if (!response.isSuccessful) {
            throw Exception("Provider fallido: ${response.code()} - ${response.message()}")
        }
        return response.body() ?: throw Exception("Provider sin contenido")
    }

    suspend fun layers(): List<MapLayer> {
        val response = api.layers()
        if (!response.isSuccessful) {
            throw Exception("Layers fallido: ${response.code()} - ${response.message()}")
        }
        return response.body() ?: emptyList()
    }

    suspend fun nearbyReports(latitude: Double, longitude: Double, radiusMeters: Int): NearbyReportsResponse {
        val response = api.nearbyReports(latitude = latitude, longitude = longitude, radiusMeters = radiusMeters)
        if (!response.isSuccessful) {
            throw Exception("Nearby reports fallido: ${response.code()} - ${response.message()}")
        }
        return response.body() ?: throw Exception("Nearby reports sin contenido")
    }
}
