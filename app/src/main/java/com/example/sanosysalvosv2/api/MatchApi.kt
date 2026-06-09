package com.example.sanosysalvosv2.api

import retrofit2.http.GET
import retrofit2.http.Header

data class MatchResult(
    val matchId: String,
    val score: Int,
    val matchedReportId: String,
    val timestamp: Long,
)

interface MatchApi {
    @GET("/api:sanos-y-salvos-coincidencias/my-matches")
    suspend fun getMyMatches(@Header("Authorization") token: String): List<MatchResult>
}
