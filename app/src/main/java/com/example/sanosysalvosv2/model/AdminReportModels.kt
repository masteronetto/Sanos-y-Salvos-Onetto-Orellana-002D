package com.example.sanosysalvosv2.model

data class AdminReportSummaryResponse(
    val id: String,
    val petName: String?,
    val reporterName: String?,
    val comuna: String?,
    val createdAt: String?,
    val type: String?,
    val status: String?,
)

data class AdminReportDetailResponse(
    val id: String,
    val petName: String?,
    val reporterName: String?,
    val comuna: String?,
    val createdAt: String?,
    val type: String?,
    val status: String?,
    val description: String?,
)

data class AdminReportSummary(
    val id: String,
    val name: String,
    val reportedBy: String,
    val comuna: String,
    val date: String,
    val caseType: String,
    val status: String,
)
