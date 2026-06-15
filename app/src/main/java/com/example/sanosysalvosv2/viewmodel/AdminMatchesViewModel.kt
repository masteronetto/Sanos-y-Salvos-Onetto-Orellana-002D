package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminCoincidenciasRepository
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminCoincidenciaDetailResponse
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AdminMatchesUiState {
    object Loading : AdminMatchesUiState()
    data class Success(val matches: List<AdminCoincidenciaSummary>) : AdminMatchesUiState()
    data class Error(val message: String) : AdminMatchesUiState()
}

sealed class AdminMatchDetailUiState {
    object Loading : AdminMatchDetailUiState()
    data class Success(val match: AdminCoincidenciaDetailResponse) : AdminMatchDetailUiState()
    data class Error(val message: String) : AdminMatchDetailUiState()
}

class AdminMatchesViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "AdminMatchesVM"

    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = AdminCoincidenciasRepository()

    private val _uiState = MutableStateFlow<AdminMatchesUiState>(AdminMatchesUiState.Loading)
    val uiState: StateFlow<AdminMatchesUiState> = _uiState.asStateFlow()

    private val _selectedMatch = MutableStateFlow<AdminCoincidenciaDetailResponse?>(null)
    val selectedMatch: StateFlow<AdminCoincidenciaDetailResponse?> = _selectedMatch.asStateFlow()

    fun loadAllMatches() {
        viewModelScope.launch {
            _uiState.value = AdminMatchesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminMatchesUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.listMatches(token)) {
                is MapsResult.Success -> _uiState.value = AdminMatchesUiState.Success(result.data)
                is MapsResult.Error -> {
                    Log.e(tag, "loadAllMatches failed: ${result.message}")
                    _uiState.value = AdminMatchesUiState.Error(result.message)
                }
            }
        }
    }

    fun confirmMatch(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminMatchesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminMatchesUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.confirmMatch(token, id)) {
                is MapsResult.Success -> loadAllMatches()
                is MapsResult.Error -> _uiState.value = AdminMatchesUiState.Error(result.message)
            }
        }
    }

    fun discardMatch(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminMatchesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AdminMatchesUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.discardMatch(token, id)) {
                is MapsResult.Success -> loadAllMatches()
                is MapsResult.Error -> _uiState.value = AdminMatchesUiState.Error(result.message)
            }
        }
    }

    fun loadMatchDetail(matchId: String) {
        viewModelScope.launch {
            _selectedMatch.value = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _selectedMatch.value = null
                return@launch
            }

            when (val result = repository.getMatchDetails(token, matchId)) {
                is MapsResult.Success -> _selectedMatch.value = result.data
                is MapsResult.Error -> {
                    Log.e(tag, "loadMatchDetail failed: ${result.message}")
                    _selectedMatch.value = null
                }
            }
        }
    }
}
