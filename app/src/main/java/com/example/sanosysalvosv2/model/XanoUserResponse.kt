package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class XanoUserResponse(
    @SerializedName("id")           val id: Long? = null,
    @SerializedName("name")         val name: String? = null,
    @SerializedName("fullName")     val fullName: String? = null,
    @SerializedName("email")        val email: String? = null,
    @SerializedName("phone")        val phone: String? = null,
    @SerializedName("phoneNumber")  val phoneNumber: String? = null,
    @SerializedName("role")         val role: String? = null,
    @SerializedName("status")       val status: String? = null,
)
