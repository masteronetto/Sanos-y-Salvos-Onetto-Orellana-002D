package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class UserMatchResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("score")
    val score: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName(value = "lostReportId", alternate = ["lost_report_id"])
    val lostReportId: String? = null,
    @SerializedName(value = "foundReportId", alternate = ["found_report_id"])
    val foundReportId: String? = null,
    @SerializedName(value = "lostPetName", alternate = ["lost_pet_name"])
    val lostPetName: String? = null,
    @SerializedName(value = "foundPetName", alternate = ["found_pet_name"])
    val foundPetName: String? = null,
    @SerializedName(value = "lostPetPhotoUrl", alternate = ["lost_pet_photo_url"])
    val lostPetPhotoUrl: String? = null,
    @SerializedName(value = "foundPetPhotoUrl", alternate = ["found_pet_photo_url"])
    val foundPetPhotoUrl: String? = null,
    @SerializedName(value = "lostPetPhotoBase64", alternate = ["lost_pet_photo_base64"])
    val lostPetPhotoBase64: String? = null,
    @SerializedName(value = "foundPetPhotoBase64", alternate = ["found_pet_photo_base64"])
    val foundPetPhotoBase64: String? = null,
    @SerializedName(value = "lostDate", alternate = ["lost_date", "event_date"])
    val lostDate: String? = null,
    @SerializedName(value = "foundDate", alternate = ["found_date"])
    val foundDate: String? = null,
    @SerializedName(value = "lostComuna", alternate = ["lost_comuna"])
    val lostComuna: String? = null,
    @SerializedName(value = "foundComuna", alternate = ["found_comuna"])
    val foundComuna: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    @SerializedName("notified")
    val notified: Boolean? = null,
)

data class UserMatchesResponse(
    @SerializedName(value = "items", alternate = ["data", "results", "list"])
    val items: List<UserMatchResponse>? = null,
    @SerializedName("total")
    val total: Int? = null,
    @SerializedName("page")
    val page: Int? = null,
)
