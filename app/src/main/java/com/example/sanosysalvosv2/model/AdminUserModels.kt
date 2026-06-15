package com.example.sanosysalvosv2.model

data class AdminCreateUserRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: String,
    val status: String,
)

data class AdminUserDetail(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val status: String,
)
