package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class PetRequest(
    val name: String,
    val species: String,
    val breed: String,
    @SerializedName("gender")
    val sex: String,
    @SerializedName("ageYears")
    val age: Int,
    val color: String,
    val isNeutered: Boolean,
    @SerializedName("isMicrochipped")
    val hasMicrochip: Boolean = false,
    val size: String = "Mediano",
    @SerializedName("photoBase64")
    val photoBase64: String = "",
    val ownerId: String = "",
)

data class PetResponse(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    @SerializedName("gender")
    val sex: String,
    @SerializedName("ageYears")
    val age: Int,
    val color: String,
    val isNeutered: Boolean,
    val size: String? = null,
    @SerializedName("isMicrochipped")
    val hasMicrochip: Boolean = false,
    @SerializedName("photoBase64")
    val photoBase64: String = "",
    val photoUrl: String?,
    val ownerId: String,
)
