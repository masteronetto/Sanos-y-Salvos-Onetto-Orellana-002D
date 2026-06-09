package com.example.sanosysalvosv2.model

data class PetReportRequest(
    val name: String,
    val status: String,       // "LOST" or "FOUND"
    val species: String,      // "dog", "cat", "other"
    val breed: String,
    val color: String,
    val size: String,         // "SMALL", "MEDIUM", "LARGE"
    val lat: Double,
    val lng: Double,
    val description: String,
    val photoBase64: String,
)

data class PetReportResponse(
    val id: String,
    val status: String,
)
