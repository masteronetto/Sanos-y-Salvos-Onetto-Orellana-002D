package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.UserReportsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.model.ReportResponse
import com.example.sanosysalvosv2.model.ReportTypeMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class UserReportsUiState {
    object Idle : UserReportsUiState()
    object Loading : UserReportsUiState()
    data class Created(val report: ReportResponse) : UserReportsUiState()
    object Updated : UserReportsUiState()
    object Deleted : UserReportsUiState()
    data class Success(val reports: List<ReportResponse>) : UserReportsUiState()
    data class Error(val message: String) : UserReportsUiState()
}

class UserReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "UserReportsViewModel"
    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = UserReportsRepository()

    private val _uiState = MutableStateFlow<UserReportsUiState>(UserReportsUiState.Idle)
    val uiState: StateFlow<UserReportsUiState> = _uiState.asStateFlow()

    private val _activeFilter = MutableStateFlow<String?>(null)
    val activeFilter: StateFlow<String?> = _activeFilter.asStateFlow()

    private var _allReports = listOf<ReportResponse>()

    private val _selectedReport = MutableStateFlow<ReportResponse?>(null)
    val selectedReport: StateFlow<ReportResponse?> = _selectedReport.asStateFlow()

    init {
        Log.d("DEBUG_VM", "ViewModel created. Initial state: ${_uiState.value}")
    }

    fun setFilter(filter: String?) {
        val normalizedFilter = ReportTypeMapper.normalizeType(filter)
        if (_activeFilter.value == normalizedFilter) return
        _activeFilter.value = normalizedFilter
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (_activeFilter.value == null) {
            _allReports
        } else {
            _allReports.filter { ReportTypeMapper.normalizeType(it.type) == _activeFilter.value }
        }
        _uiState.value = UserReportsUiState.Success(filtered)
    }

    fun loadAllReports(type: String? = null, page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.getAllReports(token, type = type, page = page)) {
                is MapsResult.Success -> {
                    _allReports = result.data
                    applyFilter()
                }
                is MapsResult.Error -> {
                    _uiState.value = UserReportsUiState.Error(result.message)
                }
            }
        }
    }

    fun searchNearby(lat: Double, lng: Double, radiusMeters: Int = 5000) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.searchNearbyReports(token, lat, lng, radiusMeters)) {
                is MapsResult.Success -> {
                    _allReports = result.data
                    _uiState.value = UserReportsUiState.Success(result.data)
                }
                is MapsResult.Error -> {
                    _uiState.value = UserReportsUiState.Error(result.message)
                }
            }
        }
    }

    fun loadMyReports() {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            when (val result = repository.listMyReports(token)) {
                is MapsResult.Success -> {
                    _allReports = result.data
                    applyFilter()
                }
                is MapsResult.Error -> {
                    Log.e(tag, "loadMyReports failed: ${result.message}")
                    _uiState.value = UserReportsUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = UserReportsUiState.Idle
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            when (val result = repository.deleteReport(token, reportId)) {
                is MapsResult.Success -> _uiState.value = UserReportsUiState.Deleted
                is MapsResult.Error -> _uiState.value = UserReportsUiState.Error(result.message)
            }
        }
    }

    fun createReport(request: ReportRequest) {
        if (_uiState.value is UserReportsUiState.Loading) return

        viewModelScope.launch {
            Log.d(tag, "createReport called, type=${request.type}")
            _uiState.value = UserReportsUiState.Loading

            val token = try {
                sessionStore.tokenFlow.first()
            } catch (e: Exception) {
                Log.e(tag, "Token retrieval failed", e)
                _uiState.value = UserReportsUiState.Error("Error de sesión")
                return@launch
            }

            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión no válida. Inicia sesión nuevamente.")
                return@launch
            }

            val result = try {
                repository.createReport(token, request)
            } catch (e: Exception) {
                Log.e(tag, "Exception creating report", e)
                _uiState.value = UserReportsUiState.Error(e.message ?: "Error inesperado")
                return@launch
            }

            when (result) {
                is MapsResult.Success -> {
                    Log.d(tag, "Report created: ${result.data.id}")
                    _uiState.value = UserReportsUiState.Created(result.data)
                }
                is MapsResult.Error -> {
                    Log.e(tag, "Error: ${result.message}")
                    _uiState.value = UserReportsUiState.Error(result.message)
                }
            }
        }
    }

    fun updateReport(reportId: String, description: String, locationName: String, eventDate: String) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            val fields = mapOf(
                "description" to description.trim(),
                "locationName" to locationName.trim(),
                "eventDate" to eventDate.trim(),
            )

            when (val result = repository.updateReportFields(token, reportId, fields)) {
                is MapsResult.Success -> {
                    _uiState.value = UserReportsUiState.Updated
                    loadMyReports()
                    loadReportDetails(reportId)
                }
                is MapsResult.Error -> _uiState.value = UserReportsUiState.Error(result.message)
            }
        }
    }

    fun markAsResolved(reportId: String) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            when (val result = repository.markAsResolved(token, reportId)) {
                is MapsResult.Success -> {
                    loadMyReports()
                    loadReportDetails(reportId)
                }
                is MapsResult.Error -> _uiState.value = UserReportsUiState.Error(result.message)
            }
        }
    }

    fun updateReport(reportId: String, request: ReportRequest) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            when (val result = repository.updateReport(token, reportId, request)) {
                is MapsResult.Success -> {
                    _uiState.value = UserReportsUiState.Updated
                    loadMyReports()
                    loadReportDetails(reportId)
                }
                is MapsResult.Error -> _uiState.value = UserReportsUiState.Error(result.message)
            }
        }
    }

    fun updateReportFields(reportId: String, fields: Map<String, Any?>) {
        viewModelScope.launch {
            _uiState.value = UserReportsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            when (val result = repository.updateReportFields(token, reportId, fields)) {
                is MapsResult.Success -> {
                    _uiState.value = UserReportsUiState.Updated
                    loadMyReports()
                    loadReportDetails(reportId)
                }
                is MapsResult.Error -> _uiState.value = UserReportsUiState.Error(result.message)
            }
        }
    }

    fun loadReportDetails(reportId: String) {
        viewModelScope.launch {
            _selectedReport.value = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = UserReportsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }

            when (val result = repository.getReportDetails(token, reportId)) {
                is MapsResult.Success -> _selectedReport.value = result.data
                is MapsResult.Error -> {
                    Log.e(tag, "loadReportDetails failed: ${result.message}")
                    _uiState.value = UserReportsUiState.Error(result.message)
                }
            }
        }
    }
}
