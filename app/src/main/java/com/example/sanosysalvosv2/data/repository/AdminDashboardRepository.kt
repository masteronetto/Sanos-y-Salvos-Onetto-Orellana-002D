package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.google.gson.JsonElement
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Dashboard metrics data class
 */
data class DashboardMetrics(
    val activeUsers: Int = 0,
    val openReports: Int = 0,
    val matchesToday: Int = 0,
    val entities: Int = 0,
    val weeklyActivity: Map<String, Int> = emptyMap(), // day -> count
)

class AdminDashboardRepository {
    private val tag = "AdminDashboardRepo"
    private val usersRepo = AdminUsersRepository()
    private val reportsRepo = AdminReportsRepository()
    private val matchesRepo = AdminCoincidenciasRepository()
    private val collaboratorsRepo = CollaboratorsRepository()

    suspend fun fetchDashboardMetrics(token: String): MapsResult<DashboardMetrics> {
        return try {
            val metrics = coroutineScope {
                val activeUsersDeferred = async { getActiveUsersCount(token) }
                val openReportsDeferred = async { getOpenReportsCount(token) }
                val matchesTodayDeferred = async { getMatchesTodayCount(token) }
                val entitiesDeferred = async { getEntitiesCount(token) }
                val weeklyActivityDeferred = async { getWeeklyActivity(token) }

                val activeUsers = activeUsersDeferred.await()
                val openReports = openReportsDeferred.await()
                val matchesToday = matchesTodayDeferred.await()
                val entities = entitiesDeferred.await()
                val weeklyActivity = weeklyActivityDeferred.await()

                DashboardMetrics(
                    activeUsers = activeUsers,
                    openReports = openReports,
                    matchesToday = matchesToday,
                    entities = entities,
                    weeklyActivity = weeklyActivity,
                )
            }
            MapsResult.Success(metrics)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching dashboard metrics", e)
            MapsResult.Error(e.message ?: "Error desconocido al cargar métricas del panel")
        }
    }

    private suspend fun getActiveUsersCount(token: String): Int {
        return try {
            val users = usersRepo.listUsers(token)
            Log.d(tag, "Users statuses from Xano: ${users.map { it.status }}")
            users.count { it.status.equals("ACTIVE", ignoreCase = true) }
        } catch (e: Exception) {
            Log.w(tag, "Error counting active users", e)
            0
        }
    }

    private suspend fun getOpenReportsCount(token: String): Int {
        return try {
            when (val result = reportsRepo.listReports(token, null, "PENDING", null)) {
                is MapsResult.Success -> result.data.size
                is MapsResult.Error -> {
                    Log.w(tag, "Error getting open reports: ${result.message}")
                    0
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error counting open reports", e)
            0
        }
    }

    private suspend fun getMatchesTodayCount(token: String): Int {
        return try {
            when (val result = matchesRepo.listMatches(token)) {
                is MapsResult.Success -> {
                    val today = LocalDate.now()
                    val dateFormatter = DateTimeFormatter.ISO_DATE
                    result.data.count { match ->
                        try {
                            // match.date is already formatted as createdAt string
                            if (match.date.length >= 10) {
                                val matchDate = LocalDate.parse(match.date.substring(0, 10), dateFormatter)
                                matchDate == today
                            } else {
                                false
                            }
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
                is MapsResult.Error -> {
                    Log.w(tag, "Error getting matches today: ${result.message}")
                    0
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error counting matches today", e)
            0
        }
    }

    private suspend fun getEntitiesCount(token: String): Int {
        return try {
            val list = collaboratorsRepo.listCollaborators(token)
            Log.d(tag, "Entities count from Xano: ${list.size}")
            list.size
        } catch (e: Exception) {
            Log.w(tag, "Error counting entities", e)
            0
        }
    }

    private suspend fun getWeeklyActivity(token: String): Map<String, Int> {
        return try {
            when (val result = reportsRepo.listReports(token, null, null, null)) {
                is MapsResult.Success -> {
                    val reports = result.data
                    val today = LocalDate.now()
                    val weekStart = today.minusDays(6)
                    val dateFormatter = DateTimeFormatter.ISO_DATE
                    val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    
                    val activityMap = mutableMapOf<String, Int>()
                    
                    // Initialize with all days of the week
                    for (i in 0..6) {
                        val dayDate = weekStart.plusDays(i.toLong())
                        val dayName = dayNames[i]
                        activityMap[dayName] = 0
                    }
                    
                    // Count reports by day
                    reports.forEach { report ->
                        try {
                            val createdAtStr = report.createdAt?.takeIf { it.length >= 10 } ?: return@forEach
                            val reportDate = LocalDate.parse(
                                createdAtStr.substring(0, 10),
                                dateFormatter
                            )
                            if (reportDate >= weekStart && reportDate <= today) {
                                val dayIndex = (reportDate.toEpochDay() - weekStart.toEpochDay()).toInt()
                                if (dayIndex >= 0 && dayIndex < 7) {
                                    val dayName = dayNames[dayIndex]
                                    activityMap[dayName] = (activityMap[dayName] ?: 0) + 1
                                }
                            }
                        } catch (e: Exception) {
                            // Skip reports with invalid dates
                        }
                    }
                    activityMap
                }
                is MapsResult.Error -> {
                    Log.w(tag, "Error getting weekly activity: ${result.message}")
                    mapOf(
                        "Lun" to 0,
                        "Mar" to 0,
                        "Mié" to 0,
                        "Jue" to 0,
                        "Vie" to 0,
                        "Sáb" to 0,
                        "Dom" to 0,
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error calculating weekly activity", e)
            mapOf(
                "Lun" to 0,
                "Mar" to 0,
                "Mié" to 0,
                "Jue" to 0,
                "Vie" to 0,
                "Sáb" to 0,
                "Dom" to 0,
            )
        }
    }
}
