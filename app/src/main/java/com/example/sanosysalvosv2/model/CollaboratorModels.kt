package com.example.sanosysalvosv2.model

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
    val id: String,
    val name: String,
    val type: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val status: String,
    val address: String? = null,
)
