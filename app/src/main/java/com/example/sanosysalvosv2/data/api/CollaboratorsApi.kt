package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.CollaboratorRequest
import com.example.sanosysalvosv2.model.CollaboratorResponse
import retrofit2.Response
import retrofit2.http.*

interface CollaboratorsApi {
    @GET("list")
    suspend fun listCollaborators(
        @Header("Authorization") authHeader: String
    ): Response<com.example.sanosysalvosv2.model.CollaboratorsListWrapper>

    @GET("details/{id}")
    suspend fun getCollaboratorDetail(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<CollaboratorResponse>

    @GET("list_by_type")
    suspend fun listCollaboratorsByType(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String,
    ): Response<com.example.sanosysalvosv2.model.CollaboratorsListWrapper>

    @POST("create")
    suspend fun createCollaborator(
        @Header("Authorization") authHeader: String,
        @Body request: CollaboratorRequest,
    ): Response<CollaboratorResponse>

    @PUT("update/{id}")
    suspend fun updateCollaborator(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body request: CollaboratorRequest,
    ): Response<CollaboratorResponse>

    @DELETE("delete/{id}")
    suspend fun deleteCollaborator(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Unit>
}
