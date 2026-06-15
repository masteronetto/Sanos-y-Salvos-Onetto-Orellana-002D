package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.data.api.MatchesRetrofitClient
import com.example.sanosysalvosv2.data.api.UserMatchesApi
import com.example.sanosysalvosv2.model.MatchResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.IOException
import retrofit2.HttpException

class UserMatchesRepository {
    private val tag = "UserMatchesRepository"

    private fun api(): UserMatchesApi = MatchesRetrofitClient.retrofit.create(UserMatchesApi::class.java)

    suspend fun getMyMatches(token: String): MapsResult<List<MatchResponse>> {
        return try {
            val response = api().getMyMatches("Bearer $token")
            if (response.isSuccessful) {
                val body: JsonElement? = response.body()
                val list = parseMatchesJson(body)
                MapsResult.Success(list)
            } else if (response.code() == 401) {
                MapsResult.Error("Sesión expirada")
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: IOException) {
            Log.e(tag, "network error", e)
            MapsResult.Error("Error de red: ${e.message}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP: ${e.code()} - ${e.message}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    suspend fun getAllMatches(token: String): MapsResult<List<MatchResponse>> {
        return try {
            val response = api().getAllMatches("Bearer $token")
            if (response.isSuccessful) {
                val body: JsonElement? = response.body()
                val list = parseMatchesJson(body)
                MapsResult.Success(list)
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    private fun parseMatchesJson(body: JsonElement?): List<MatchResponse> {
        if (body == null || body.isJsonNull) return emptyList()
        val gson = Gson()
        return try {
            if (body.isJsonArray) {
                gson.fromJson(body, object : TypeToken<List<MatchResponse>>() {}.type)
            } else if (body.isJsonObject) {
                val obj = body.asJsonObject
                val candidateKeys = listOf("data", "results", "items", "matches", "list")
                for (key in candidateKeys) {
                    if (obj.has(key)) {
                        val el = obj.get(key)
                        if (el.isJsonArray) {
                            return gson.fromJson(el, object : TypeToken<List<MatchResponse>>() {}.type)
                        }
                    }
                }

                // Fallback: try to parse object as single MatchResponse
                val single = gson.fromJson(body, MatchResponse::class.java)
                listOf(single)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse matches JSON", e)
            emptyList()
        }
    }

    suspend fun getMatchDetails(token: String, matchId: String): MapsResult<MatchResponse> {
        return try {
            val response = api().getMatchDetails("Bearer $token", matchId)
            if (response.isSuccessful) {
                MapsResult.Success(response.body()!!)
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    suspend fun acceptMatch(token: String, matchId: String): MapsResult<MatchResponse> {
        return try {
            val response = api().acceptMatch("Bearer $token", matchId)
            if (response.isSuccessful) {
                MapsResult.Success(response.body()!!)
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }

    suspend fun rejectMatch(token: String, matchId: String): MapsResult<MatchResponse> {
        return try {
            val response = api().rejectMatch("Bearer $token", matchId)
            if (response.isSuccessful) {
                MapsResult.Success(response.body()!!)
            } else {
                MapsResult.Error("Error ${response.code()}")
            }
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado")
        }
    }
}
