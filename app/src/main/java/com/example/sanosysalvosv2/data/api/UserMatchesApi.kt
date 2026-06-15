package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.MatchResponse
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserMatchesApi {
    @GET("my_matches")
    suspend fun getMyMatches(@Header("Authorization") auth: String): Response<JsonElement>

    @GET("list")
    suspend fun getAllMatches(@Header("Authorization") auth: String): Response<JsonElement>

    @GET("details/{id}")
    suspend fun getMatchDetails(@Header("Authorization") auth: String, @Path("id") id: String): Response<MatchResponse>

    @GET("pending")
    suspend fun getPendingMatches(@Header("Authorization") auth: String): Response<JsonElement>

    @PUT("accept/{id}")
    suspend fun acceptMatch(@Header("Authorization") auth: String, @Path("id") id: String): Response<MatchResponse>

    @PUT("reject/{id}")
    suspend fun rejectMatch(@Header("Authorization") auth: String, @Path("id") id: String): Response<MatchResponse>
}
