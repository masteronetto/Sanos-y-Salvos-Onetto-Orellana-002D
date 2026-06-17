package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminDashboardRepository
import com.example.sanosysalvosv2.data.repository.DashboardMetrics
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.util.ErrorHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminDashboardRepository()
    private val sessionStore = SessionStore(application.applicationContext)
    private val tag = "AdminDashboardVM"

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var metrics by mutableStateOf(
        DashboardMetrics(
            activeUsers = 0,
            openReports = 0,
            matchesToday = 0,
            entities = 0,
            weeklyActivity = mapOf(
                "Lun" to 0,
                "Mar" to 0,
                "Mié" to 0,
                "Jue" to 0,
                "Vie" to 0,
                "Sáb" to 0,
                "Dom" to 0,
            ),
        )
    )
        private set

    fun loadMetrics() {
        loading = true
        error = null

        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    throw IllegalStateException("Sesión inválida")
                }

                when (val result = repository.fetchDashboardMetrics(token)) {
                    is MapsResult.Success -> {
                        metrics = result.data
                        Log.d(tag, "Metrics loaded: users=${metrics.activeUsers}, reports=${metrics.openReports}, matches=${metrics.matchesToday}, entities=${metrics.entities}")
                    }
                    is MapsResult.Error -> {
                        error = result.message
                        Log.e(tag, "Error loading metrics: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                error = ErrorHandler.getErrorMessage(e)
                Log.e(tag, "Exception loading metrics", e)
            } finally {
                loading = false
            }
        }
    }
}
