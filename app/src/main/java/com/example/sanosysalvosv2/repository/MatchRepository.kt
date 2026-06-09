package com.example.sanosysalvosv2.repository

import com.example.sanosysalvosv2.api.MatchApi
import com.example.sanosysalvosv2.api.MatchResult
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient

sealed interface MatchResult_<T> {
    data class Success<T>(val data: T) : MatchResult_<T>
    data class Error<T>(val exception: Throwable) : MatchResult_<T>
}

class MatchRepository {
    private val matchApi = XanoRetrofitClient.retrofit.create(MatchApi::class.java)

    suspend fun getMyMatches(token: String): MatchResult_<List<MatchResult>> = try {
        val response = matchApi.getMyMatches("Bearer $token")
        MatchResult_.Success(response)
    } catch (e: Exception) {
        MatchResult_.Error(e)
    }
}
