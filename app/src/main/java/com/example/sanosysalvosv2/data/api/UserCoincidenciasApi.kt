package com.example.sanosysalvosv2.data.api

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserCoincidenciasApi {
    @GET("/api:sanos-y-salvos-matches/my_matches")
    suspend fun getMyMatches(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
    ): Response<JsonElement>

    @PUT("/api:sanos-y-salvos-matches/accept/{id}")
    suspend fun acceptMatch(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<JsonElement>

    @PUT("/api:sanos-y-salvos-matches/reject/{id}")
    suspend fun rejectMatch(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<JsonElement>
}
