package com.example.sanosysalvosv2.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String = "USER",
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class AuthResponse(
    val userId: String,
    val role: String,
    val token: String,
)

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
)
