package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.PetsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.PetRequest
import com.example.sanosysalvosv2.model.PetResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AdminMascotasUiState {
    object Loading : AdminMascotasUiState
    data class Success(val pets: List<PetResponse>) : AdminMascotasUiState
    data class Error(val message: String) : AdminMascotasUiState
}

class AdminMascotasViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "AdminMascotasVM"
    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = PetsRepository()

    private val _uiState = MutableStateFlow<AdminMascotasUiState>(AdminMascotasUiState.Loading)
    val uiState: StateFlow<AdminMascotasUiState> = _uiState.asStateFlow()

    private val _selectedPet = MutableStateFlow<PetResponse?>(null)
    val selectedPet: StateFlow<PetResponse?> = _selectedPet.asStateFlow()

    fun loadAllPets(page: Int = 1, perPage: Int = 20, species: String = "", breed: String = "") {
        _uiState.value = AdminMascotasUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                when (val result = repository.listPets(token, page = page, perPage = perPage, species = species, breed = breed)) {
                    is MapsResult.Success -> _uiState.value = AdminMascotasUiState.Success(result.data)
                    is MapsResult.Error -> _uiState.value = AdminMascotasUiState.Error(result.message)
                }
            } catch (e: Exception) {
                Log.e(tag, "loadAllPets failed", e)
                _uiState.value = AdminMascotasUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun loadPetDetails(id: String) {
        viewModelScope.launch {
            try {
                _selectedPet.value = null
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                when (val result = repository.getPetDetails(token, id)) {
                    is MapsResult.Success -> _selectedPet.value = result.data
                    is MapsResult.Error -> _uiState.value = AdminMascotasUiState.Error(result.message)
                }
            } catch (e: Exception) {
                Log.e(tag, "loadPetDetails failed", e)
                _uiState.value = AdminMascotasUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createPet(request: PetRequest) {
        _uiState.value = AdminMascotasUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                when (val result = repository.createPet(token, request)) {
                    is MapsResult.Success -> loadAllPets()
                    is MapsResult.Error -> _uiState.value = AdminMascotasUiState.Error(result.message)
                }
            } catch (e: Exception) {
                Log.e(tag, "createPet failed", e)
                _uiState.value = AdminMascotasUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun updatePet(id: String, request: PetRequest) {
        _uiState.value = AdminMascotasUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                when (val result = repository.updatePet(token, id, request)) {
                    is MapsResult.Success -> loadAllPets()
                    is MapsResult.Error -> _uiState.value = AdminMascotasUiState.Error(result.message)
                }
            } catch (e: Exception) {
                Log.e(tag, "updatePet failed", e)
                _uiState.value = AdminMascotasUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun deletePet(id: String) {
        _uiState.value = AdminMascotasUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                when (val result = repository.deletePet(token, id)) {
                    is MapsResult.Success -> loadAllPets()
                    is MapsResult.Error -> _uiState.value = AdminMascotasUiState.Error(result.message)
                }
            } catch (e: Exception) {
                Log.e(tag, "deletePet failed", e)
                _uiState.value = AdminMascotasUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }
}
