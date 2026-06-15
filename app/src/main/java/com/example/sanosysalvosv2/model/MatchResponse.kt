package com.example.sanosysalvosv2.model

data class MatchResponse(
    val id: String,
    val lostReportId: String,
    val foundReportId: String,
    val score: Int,
    val reason: String = "",
    val status: String,
    val createdAt: Long? = null,
)
