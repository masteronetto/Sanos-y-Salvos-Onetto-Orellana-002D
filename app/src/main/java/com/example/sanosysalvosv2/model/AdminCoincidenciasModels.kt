package com.example.sanosysalvosv2.model

data class AdminCoincidenciaSummaryResponse(
    val id: String,
    val sourcePetName: String?,
    val matchedPetName: String?,
    val score: Int?,
    val status: String?,
    val comuna: String?,
    val createdAt: String?,
)

data class AdminCoincidenciaDetailResponse(
    val id: String,
    val sourcePetName: String?,
    val matchedPetName: String?,
    val sourcePetPhotoUrl: String?,
    val matchedPetPhotoUrl: String?,
    val score: Int?,
    val status: String?,
    val comuna: String?,
    val createdAt: String?,
    val details: String?,
)

data class AdminCoincidenciaSummary(
    val id: String,
    val sourceName: String,
    val matchedName: String,
    val score: Int,
    val status: String,
    val comuna: String,
    val date: String,
)
