package com.example.sanosysalvosv2.model

data class UserProfileResponse(
    val id: String,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val role: String? = null,
    val uid: String? = null,
    val status: String? = null,
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val city: String,
    val role: String = "",
    val uid: String = "",
    val status: String = "",
)

data class UpdateProfileRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val deviceToken: String? = null,
)
