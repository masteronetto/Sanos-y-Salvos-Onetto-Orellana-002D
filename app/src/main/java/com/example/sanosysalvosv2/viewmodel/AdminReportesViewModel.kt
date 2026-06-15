package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminReportesRepository
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminReportSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AdminReportesUiState {
    object Loading : AdminReportesUiState()
    data class Success(val reports: List<AdminReportSummary>) : AdminReportesUiState()
    data class Error(val message: String) : AdminReportesUiState()
}

class AdminReportesViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "AdminReportesVM"

    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = AdminReportesRepository()

    private val _uiState = MutableStateFlow<AdminReportesUiState>(AdminReportesUiState.Loading)
    val uiState: StateFlow<AdminReportesUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    fun setSelectedFilter(value: String) {
        if (_selectedFilter.value == value) return
        _selectedFilter.value = value
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = AdminReportesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportesUiState.Error("Sesión no válida")
                return@launch
            }

            val type = when (_selectedFilter.value) {
                "LOST" -> "LOST"
                "FOUND" -> "FOUND"
                else -> null
            }

            when (val result = repository.listReports(token, type = type, status = null, comuna = null)) {
                is MapsResult.Success -> _uiState.value = AdminReportesUiState.Success(result.data)
                is MapsResult.Error -> {
                    Log.e(tag, "loadReports failed: ${result.message}")
                    _uiState.value = AdminReportesUiState.Error(result.message)
                }
            }
        }
    }

    fun approveReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = AdminReportesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportesUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.approveReport(token, reportId)) {
                is MapsResult.Success -> loadReports()
                is MapsResult.Error -> _uiState.value = AdminReportesUiState.Error(result.message)
            }
        }
    }

    fun rejectReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = AdminReportesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminReportesUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.rejectReport(token, reportId)) {
                is MapsResult.Success -> loadReports()
                is MapsResult.Error -> _uiState.value = AdminReportesUiState.Error(result.message)
            }
        }
    }
}
