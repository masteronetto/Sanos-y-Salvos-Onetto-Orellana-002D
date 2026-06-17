package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.AdminDashboardStats
import com.example.sanosysalvosv2.model.AdminStatsResponse
import com.example.sanosysalvosv2.model.RecoveryStats
import com.example.sanosysalvosv2.model.ReportsPerCommune
import retrofit2.Response

class AdminStatsRepository {
    private val tag = "AdminStatsRepo"

    private fun api(): com.example.sanosysalvosv2.data.api.AdminStatsApi =
        XanoRetrofitClient.retrofit.create(com.example.sanosysalvosv2.data.api.AdminStatsApi::class.java)

    suspend fun fetchDashboard(token: String): Result<AdminDashboardStats> {
        return try {
            val response = api().getDashboard()
            if (!response.isSuccessful) throw RuntimeException("Dashboard error ${response.code()}")
            val body = response.body() ?: throw RuntimeException("Empty dashboard response")
            Result.success(body.dashboard)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching dashboard", e)
            Result.failure(e)
        }
    }

    suspend fun fetchRecovery(token: String): Result<RecoveryStats> {
        return try {
            val response = api().getRecovery()
            if (!response.isSuccessful) throw RuntimeException("Recovery error ${response.code()}")
            val body = response.body() ?: throw RuntimeException("Empty recovery response")
            Result.success(body.recoveryStats)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching recovery", e)
            Result.failure(e)
        }
    }

    suspend fun fetchReportsByCommune(token: String): Result<List<ReportsPerCommune>> {
        return try {
            val response = api().getReportsByCommune()
            if (!response.isSuccessful) throw RuntimeException("Reports by commune error ${response.code()}")
            val body = response.body() ?: throw RuntimeException("Empty reports by commune response")
            Result.success(body.reportsByCommune)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching reports by commune", e)
            Result.failure(e)
        }
    }
}
