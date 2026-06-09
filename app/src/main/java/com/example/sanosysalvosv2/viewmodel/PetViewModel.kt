package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.PetRepository
import com.example.sanosysalvosv2.data.repository.PetResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.PetReportRequest
import com.example.sanosysalvosv2.util.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class PetReportUiState {
    data object Idle : PetReportUiState()
    data object Loading : PetReportUiState()
    data class Success(val id: String) : PetReportUiState()
    data class Error(val message: String) : PetReportUiState()
}

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PetRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    private val _uiState = MutableStateFlow<PetReportUiState>(PetReportUiState.Idle)
    val uiState: StateFlow<PetReportUiState> = _uiState.asStateFlow()

    fun submitReport(request: PetReportRequest) {
        _uiState.value = PetReportUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _uiState.value = PetReportUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                    return@launch
                }
                when (val result = repository.createReport(token, request)) {
                    is PetResult.Success -> _uiState.value = PetReportUiState.Success(result.data.id)
                    is PetResult.Error -> _uiState.value = PetReportUiState.Error(result.message)
                }
            } catch (e: Exception) {
                _uiState.value = PetReportUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun resetState() {
        _uiState.value = PetReportUiState.Idle
    }
}
