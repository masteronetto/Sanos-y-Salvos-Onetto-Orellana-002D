package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val eventDate: String,
    val locationName: String? = null,
    val photoBase64: String? = null,
    val species: String? = null,
    val breed: String? = null,
    val color: String? = null,
    val petId: String? = null,
    val petName: String? = null,
    val size: String? = null
)

data class ReportResponse(
    val id: String,
    val type: String,
    @SerializedName(value = "reporterId", alternate = ["reporter_id"])
    val reporterId: String?,
    val description: String?,
    val status: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName(value = "locationName", alternate = ["location_name"])
    val locationName: String?,
    @SerializedName(value = "eventDate", alternate = ["event_date"])
    val eventDate: String?,
    @SerializedName(value = "photoBase64", alternate = ["photo_base64"])
    val photoBase64: String? = null,
    @SerializedName(value = "photoUrl", alternate = ["photo_url"])
    val photoUrl: String?,
    val species: String?,
    val breed: String?,
    val color: String?,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String?,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null,
)
