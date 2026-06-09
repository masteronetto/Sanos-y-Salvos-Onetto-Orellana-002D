package com.example.sanosysalvosv2.model

data class NearbyReport(
    val lat: Double,
    val lon: Double,
    val title: String,
    val description: String,
    // status is derived from description by MapsRepository ("lost · X m" / "found · X m").
    // Populated explicitly once MapsRepository is updated to map reportType directly.
    val status: String = "",
)