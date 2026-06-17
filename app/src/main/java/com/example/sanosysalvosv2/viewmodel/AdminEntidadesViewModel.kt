package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.CollaboratorsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.CollaboratorRequest
import com.example.sanosysalvosv2.model.CollaboratorResponse
import com.example.sanosysalvosv2.util.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AdminEntidadesUiState {
    object Loading : AdminEntidadesUiState
    data class Success(val collaborators: List<CollaboratorResponse>) : AdminEntidadesUiState
    data class Error(val message: String) : AdminEntidadesUiState
}

class AdminEntidadesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CollaboratorsRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    private val _uiState = MutableStateFlow<AdminEntidadesUiState>(AdminEntidadesUiState.Loading)
    val uiState: StateFlow<AdminEntidadesUiState> = _uiState.asStateFlow()

    private val _selectedEntidad = MutableStateFlow<CollaboratorResponse?>(null)
    val selectedEntidad: StateFlow<CollaboratorResponse?> = _selectedEntidad.asStateFlow()

    private var currentCollaborators: List<CollaboratorResponse> = emptyList()

    fun loadEntidades() {
        _uiState.value = AdminEntidadesUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                currentCollaborators = repository.listCollaborators(token)
                _uiState.value = AdminEntidadesUiState.Success(currentCollaborators)
            } catch (e: Exception) {
                Log.e("AdminEntidadesVM", "loadEntidades failed: ${e.message} url=https://x8ki-letl-twmt.n7.xano.io/api:collaborators/list", e)
                _uiState.value = AdminEntidadesUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun loadByType(type: String) {
        _uiState.value = AdminEntidadesUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                currentCollaborators = repository.listCollaboratorsByType(token, type)
                _uiState.value = AdminEntidadesUiState.Success(currentCollaborators)
            } catch (e: Exception) {
                Log.e("AdminEntidadesVM", "loadByType failed", e)
                _uiState.value = AdminEntidadesUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun loadEntidadDetail(id: String) {
        _selectedEntidad.value = null
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                _selectedEntidad.value = repository.getCollaboratorDetail(token, id)
            } catch (e: Exception) {
                Log.e("AdminEntidadesVM", "loadEntidadDetail failed", e)
                _selectedEntidad.value = null
            }
        }
    }

    fun createEntidad(request: CollaboratorRequest) {
        _uiState.value = AdminEntidadesUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                repository.createCollaborator(token, request)
                loadEntidades()
            } catch (e: Exception) {
                Log.e("AdminEntidadesVM", "createEntidad failed", e)
                _uiState.value = AdminEntidadesUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun updateEntidad(id: String, request: CollaboratorRequest) {
        _uiState.value = AdminEntidadesUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                repository.updateCollaborator(token, id, request)
                loadEntidades()
            } catch (e: Exception) {
                Log.e("AdminEntidadesVM", "updateEntidad failed", e)
                _uiState.value = AdminEntidadesUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun deleteEntidad(id: String) {
        _uiState.value = AdminEntidadesUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                val deleted = repository.deleteCollaborator(token, id)
                if (!deleted) throw IllegalStateException("No se pudo eliminar la entidad")
                loadEntidades()
            } catch (e: Exception) {
                Log.e("AdminEntidadesVM", "deleteEntidad failed", e)
                _uiState.value = AdminEntidadesUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
}
