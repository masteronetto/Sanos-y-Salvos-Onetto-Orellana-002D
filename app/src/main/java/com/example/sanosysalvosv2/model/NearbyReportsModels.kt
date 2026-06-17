package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

enum class ReportType {
    LOST,
    FOUND,
}

data class NearbyReportMarker(
    @SerializedName(value = "reportId", alternate = ["report_id"])
    val reportId: String,
    val reportType: ReportType,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val distanceMeters: Double,
    @SerializedName(value = "photoUrl", alternate = ["photo_url"])
    val photoUrl: String? = null,
    @SerializedName(value = "photoBase64", alternate = ["photo_base64"])
    val photoBase64: String? = null,
    @SerializedName(value = "reporterName", alternate = ["reporter_name"])
    val reporterName: String? = null,
    @SerializedName(value = "reporterPhone", alternate = ["reporter_phone"])
    val reporterPhone: String? = null,
)

data class NearbyReportsResponse(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Int,
    val total: Int,
    val markers: List<NearbyReportMarker>,
)