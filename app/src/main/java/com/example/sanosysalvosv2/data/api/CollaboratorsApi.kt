package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.CollaboratorRequest
import com.example.sanosysalvosv2.model.CollaboratorResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CollaboratorsApi {
    @GET("/api:sanos-y-salvos-collaborators/list")
    suspend fun listCollaborators(
        @Header("Authorization") authHeader: String,
    ): Response<List<CollaboratorResponse>>

    @GET("/api:sanos-y-salvos-collaborators/details/{id}")
    suspend fun getCollaboratorDetail(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<CollaboratorResponse>

    @GET("/api:sanos-y-salvos-collaborators/list_by_type")
    suspend fun listCollaboratorsByType(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String,
    ): Response<List<CollaboratorResponse>>

    @POST("/api:sanos-y-salvos-collaborators/create")
    suspend fun createCollaborator(
        @Header("Authorization") authHeader: String,
        @Body request: CollaboratorRequest,
    ): Response<CollaboratorResponse>

    @PUT("/api:sanos-y-salvos-collaborators/update/{id}")
    suspend fun updateCollaborator(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body request: CollaboratorRequest,
    ): Response<CollaboratorResponse>

    @DELETE("/api:sanos-y-salvos-collaborators/delete/{id}")
    suspend fun deleteCollaborator(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Unit>
}
