package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.AdminCoincidenciaDetailResponse
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummaryResponse
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminCoincidenciasApi {
    @GET("/api:sanos-y-salvos-matches/list")
    suspend fun listMatches(
        @Header("Authorization") authHeader: String,
    ): Response<JsonElement>

    @GET("/api:sanos-y-salvos-matches/details/{id}")
    suspend fun getMatchDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<AdminCoincidenciaDetailResponse>

    @PUT("/api:sanos-y-salvos-matches/accept/{id}")
    suspend fun confirmMatch(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<AdminCoincidenciaDetailResponse>

    @PUT("/api:sanos-y-salvos-matches/reject/{id}")
    suspend fun discardMatch(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<AdminCoincidenciaDetailResponse>
}
