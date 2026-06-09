package com.example.sanosysalvosv2.model

// Xano expects: email, fullName, password — verified against schema 2026-06-08
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

// Xano returns: token, userId, role — verified against schema 2026-06-08
data class AuthResponse(
    val userId: String,
    // Known Xano role values: USER, ADMIN, COLLABORATOR
    val role: String,
    val token: String,
)

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
)
