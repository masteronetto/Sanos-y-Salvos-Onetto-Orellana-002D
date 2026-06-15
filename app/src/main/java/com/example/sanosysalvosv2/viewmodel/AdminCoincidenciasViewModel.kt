package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminCoincidenciasRepository
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AdminCoincidenciasUiState {
    object Loading : AdminCoincidenciasUiState()
    data class Success(val matches: List<AdminCoincidenciaSummary>) : AdminCoincidenciasUiState()
    data class Error(val message: String) : AdminCoincidenciasUiState()
}

class AdminCoincidenciasViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "AdminCoincidenciasVM"

    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = AdminCoincidenciasRepository()

    private val _uiState = MutableStateFlow<AdminCoincidenciasUiState>(AdminCoincidenciasUiState.Loading)
    val uiState: StateFlow<AdminCoincidenciasUiState> = _uiState.asStateFlow()

    private val _selectedStatus = MutableStateFlow("ALL")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    fun setSelectedStatus(status: String) {
        if (_selectedStatus.value == status) return
        _selectedStatus.value = status
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.value = AdminCoincidenciasUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminCoincidenciasUiState.Error("Sesión no válida")
                return@launch
            }

            val status = when (_selectedStatus.value) {
                "PENDING" -> "PENDING"
                "CONFIRMED" -> "CONFIRMED"
                "DISCARDED" -> "DISCARDED"
                else -> null
            }

            when (val result = repository.listMatches(token)) {
                is MapsResult.Success -> {
                    val filteredMatches = if (status != null) {
                        result.data.filter { it.status == status }
                    } else {
                        result.data
                    }
                    _uiState.value = AdminCoincidenciasUiState.Success(filteredMatches)
                }
                is MapsResult.Error -> {
                    Log.e(tag, "loadMatches failed: ${result.message}")
                    _uiState.value = AdminCoincidenciasUiState.Error(result.message)
                }
            }
        }
    }

    fun confirmMatch(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminCoincidenciasUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminCoincidenciasUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.confirmMatch(token, id)) {
                is MapsResult.Success -> loadMatches()
                is MapsResult.Error -> _uiState.value = AdminCoincidenciasUiState.Error(result.message)
            }
        }
    }

    fun discardMatch(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminCoincidenciasUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminCoincidenciasUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.discardMatch(token, id)) {
                is MapsResult.Success -> loadMatches()
                is MapsResult.Error -> _uiState.value = AdminCoincidenciasUiState.Error(result.message)
            }
        }
    }
}
