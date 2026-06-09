package com.example.sanosysalvosv2.model

enum class ReportType {
    LOST,
    FOUND,
}

data class NearbyReportMarker(
    val reportId: String,
    val reportType: ReportType,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val distanceMeters: Double,
)

data class NearbyReportsResponse(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Int,
    val total: Int,
    val markers: List<NearbyReportMarker>,
)