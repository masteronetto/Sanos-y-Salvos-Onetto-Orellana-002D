package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.data.api.CollaboratorsApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.CollaboratorRequest
import com.example.sanosysalvosv2.model.CollaboratorResponse
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CollaboratorsRepository {
    private val tag = "CollaboratorsRepo"

    private fun api(): CollaboratorsApi = XanoRetrofitClient.retrofit.create(CollaboratorsApi::class.java)

    suspend fun listCollaborators(token: String): List<CollaboratorResponse> {
        val response = api().listCollaborators(authHeader = "Bearer $token")
        return parseListResponse(response)
    }

    suspend fun getCollaboratorDetail(token: String, id: String): CollaboratorResponse {
        val response = api().getCollaboratorDetail(authHeader = "Bearer $token", id = id)
        return parseResponse(response)
    }

    suspend fun listCollaboratorsByType(token: String, type: String): List<CollaboratorResponse> {
        val response = api().listCollaboratorsByType(authHeader = "Bearer $token", type = type)
        return parseListResponse(response)
    }

    suspend fun createCollaborator(token: String, request: CollaboratorRequest): CollaboratorResponse {
        val response = api().createCollaborator(authHeader = "Bearer $token", request = request)
        return parseResponse(response)
    }

    suspend fun updateCollaborator(token: String, id: String, request: CollaboratorRequest): CollaboratorResponse {
        val response = api().updateCollaborator(authHeader = "Bearer $token", id = id, request = request)
        return parseResponse(response)
    }

    suspend fun deleteCollaborator(token: String, id: String): Boolean {
        val response = api().deleteCollaborator(authHeader = "Bearer $token", id = id)
        return response.isSuccessful
    }

    private fun parseResponse(response: Response<CollaboratorResponse>): CollaboratorResponse {
        if (!response.isSuccessful) {
            val message = response.errorBody()?.string().orEmpty().takeIf { it.isNotBlank() }
                ?: "Error ${response.code()}"
            throw HttpException(response)
        }
        return response.body() ?: throw IllegalStateException("Respuesta vacía del colaborador")
    }

    private fun parseListResponse(response: Response<List<CollaboratorResponse>>): List<CollaboratorResponse> {
        if (!response.isSuccessful) {
            val message = response.errorBody()?.string().orEmpty().takeIf { it.isNotBlank() }
                ?: "Error ${response.code()}"
            throw HttpException(response)
        }
        return response.body() ?: emptyList()
    }
}
