package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class AdminCoincidenciaSummaryResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName(value = "sourcePetName", alternate = ["source_pet_name"])
    val sourcePetName: String?,
    @SerializedName(value = "matchedPetName", alternate = ["matched_pet_name"])
    val matchedPetName: String?,
    @SerializedName("score")
    val score: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("comuna")
    val comuna: String?,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String?,
)

data class AdminCoincidenciaDetailResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName(value = "sourcePetName", alternate = ["source_pet_name"])
    val sourcePetName: String?,
    @SerializedName(value = "matchedPetName", alternate = ["matched_pet_name"])
    val matchedPetName: String?,
    @SerializedName(value = "sourcePetPhotoUrl", alternate = ["source_pet_photo_url"])
    val sourcePetPhotoUrl: String?,
    @SerializedName(value = "matchedPetPhotoUrl", alternate = ["matched_pet_photo_url"])
    val matchedPetPhotoUrl: String?,
    @SerializedName("score")
    val score: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("comuna")
    val comuna: String?,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String?,
    @SerializedName("details")
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
