package com.example.sanosysalvosv2.model

data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalPets: Int = 0,
    val totalReports: Int = 0,
    val openReports: Int = 0,
    val resolvedReports: Int = 0,
    val pendingMatches: Int = 0,
    val confirmedMatches: Int = 0,
    val totalEntidades: Int = 0,
    val averageRecoveryRate: Double = 0.0,
    val averageRecoveryTime: Int = 0,
    val reportsThisWeek: Int = 0,
    val matchesThisWeek: Int = 0,
    val topCommune: String? = null,
    val topCommuneReports: Int = 0,
)

data class RecoveryStats(
    val recoveryRate: Double = 0.0,
    val averageTimeInDays: Int = 0,
    val totalRecovered: Int = 0,
    val totalLost: Int = 0,
)

data class ReportsPerCommune(
    val communeName: String = "",
    val count: Int = 0,
)

data class AdminStatsResponse(
    val dashboard: AdminDashboardStats = AdminDashboardStats(),
    val recoveryStats: RecoveryStats = RecoveryStats(),
    val reportsByCommune: List<ReportsPerCommune> = emptyList(),
)
