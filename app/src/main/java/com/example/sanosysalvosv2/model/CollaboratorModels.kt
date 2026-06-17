package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class CollaboratorRequest(
    val name: String,
    val type: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val address: String,
    val status: String,
)

data class CollaboratorResponse(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val email: String = "",
    val phone: String = "",
    val comuna: String = "",
    val address: String = "",
    val status: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
)
