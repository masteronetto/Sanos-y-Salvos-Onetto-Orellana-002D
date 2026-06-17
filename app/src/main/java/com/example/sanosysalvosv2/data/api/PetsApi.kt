package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.model.PetRequest
import com.example.sanosysalvosv2.model.PetResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PetsApi {
    @GET("/api:sanos-y-salvos-pets/list")
    suspend fun listPets(
        @Header("Authorization") authHeader: String,
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("per_page") perPage: Int = 20,
        @retrofit2.http.Query("species") species: String? = null,
        @retrofit2.http.Query("breed") breed: String? = null,
    ): Response<Any>

    @GET("/api:sanos-y-salvos-pets/my")
    suspend fun listMyPets(
        @Header("Authorization") authHeader: String,
    ): Response<Any>

    @GET("/api:sanos-y-salvos-pets/list_by_owner/{ownerId}")
    suspend fun listPetsByOwner(
        @Header("Authorization") authHeader: String,
        @Path("ownerId") ownerId: String,
    ): Response<Any>

    @GET("/api:sanos-y-salvos-pets/details/{id}")
    suspend fun getPetDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<PetResponse>

    @POST("/api:sanos-y-salvos-pets/create")
    suspend fun createPet(
        @Header("Authorization") authHeader: String,
        @Body request: PetRequest,
    ): Response<PetResponse>

    @PUT("/api:sanos-y-salvos-pets/update/{id}")
    suspend fun updatePet(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body request: PetRequest,
    ): Response<PetResponse>

    @DELETE("/api:sanos-y-salvos-pets/delete/{id}")
    suspend fun deletePet(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Response<Map<String, Any?>>
}
