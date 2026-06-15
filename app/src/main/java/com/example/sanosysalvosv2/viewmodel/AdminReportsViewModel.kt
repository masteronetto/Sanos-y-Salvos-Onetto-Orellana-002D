package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminReportsRepository
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.ReportResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AdminReportsUiState {
    object Loading : AdminReportsUiState()
    data class Success(val reports: List<ReportResponse>) : AdminReportsUiState()
    data class Error(val message: String) : AdminReportsUiState()
}

class AdminReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "AdminReportsVM"
    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = AdminReportsRepository()

    private val _uiState = MutableStateFlow<AdminReportsUiState>(AdminReportsUiState.Loading)
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()

    private val _selectedReport = MutableStateFlow<ReportResponse?>(null)
    val selectedReport: StateFlow<ReportResponse?> = _selectedReport.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    fun setSelectedFilter(value: String) {
        if (_selectedFilter.value == value) return
        _selectedFilter.value = value
        loadReports()
    }

    fun loadReports(type: String? = null, status: String? = null, comuna: String? = null) {
        viewModelScope.launch {
            _uiState.value = AdminReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportsUiState.Error("Sesión no válida")
                return@launch
            }

            val t = type ?: when (_selectedFilter.value) {
                "LOST" -> "LOST"
                "FOUND" -> "FOUND"
                else -> null
            }

            when (val result = repository.listReports(token, t, status, comuna)) {
                is MapsResult.Success -> _uiState.value = AdminReportsUiState.Success(result.data)
                is MapsResult.Error -> {
                    Log.e(tag, "loadReports failed: ${result.message}")
                    _uiState.value = AdminReportsUiState.Error(result.message)
                }
            }
        }
    }

    fun loadReportDetails(reportId: String) {
        viewModelScope.launch {
            _selectedReport.value = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.getReportDetails(token, reportId)) {
                is MapsResult.Success -> _selectedReport.value = result.data
                is MapsResult.Error -> {
                    Log.e(tag, "loadReportDetails failed: ${result.message}")
                    _uiState.value = AdminReportsUiState.Error(result.message)
                }
            }
        }
    }

    fun approveReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = AdminReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.updateReport(token, reportId, mapOf("status" to "APPROVED"))) {
                is MapsResult.Success -> loadReports()
                is MapsResult.Error -> _uiState.value = AdminReportsUiState.Error(result.message)
            }
        }
    }

    fun rejectReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = AdminReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.updateReport(token, reportId, mapOf("status" to "REJECTED"))) {
                is MapsResult.Success -> loadReports()
                is MapsResult.Error -> _uiState.value = AdminReportsUiState.Error(result.message)
            }
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = AdminReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.deleteReport(token, reportId)) {
                is MapsResult.Success -> loadReports()
                is MapsResult.Error -> _uiState.value = AdminReportsUiState.Error(result.message)
            }
        }
    }
}
