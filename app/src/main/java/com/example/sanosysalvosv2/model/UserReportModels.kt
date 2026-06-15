package com.example.sanosysalvosv2.model

data class ReportRequest(
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val eventDate: String,
    val locationName: String?,
    val photoUrl: String?,
    val photoBase64: String? = null,
    val species: String?,
    val breed: String?,
    val color: String?,
    val petId: String? = null,
    val petName: String? = null,
    val size: String? = null,
)

data class ReportResponse(
    val id: String,
    val type: String,
    val reporterId: String?,
    val description: String?,
    val status: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val eventDate: String?,
    val photoUrl: String?,
    val species: String?,
    val breed: String?,
    val color: String?,
    val createdAt: String?,
    val updatedAt: String? = null,
)
