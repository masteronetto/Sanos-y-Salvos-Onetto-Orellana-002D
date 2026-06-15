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

sealed class PetsUiState {
    object Idle : PetsUiState()
    object Loading : PetsUiState()
    data class Success(val pets: List<PetResponse>) : PetsUiState()
    data class Error(val message: String) : PetsUiState()
    data class Deleted(val petId: String) : PetsUiState()
    data class Saved(val pet: PetResponse) : PetsUiState()
}

class PetsViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "PetsViewModel"
    private val repository = PetsRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    private val _uiState = MutableStateFlow<PetsUiState>(PetsUiState.Idle)
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    private val _selectedPet = MutableStateFlow<PetResponse?>(null)
    val selectedPet: StateFlow<PetResponse?> = _selectedPet.asStateFlow()

    fun loadMyPets() {
        viewModelScope.launch {
            _uiState.value = PetsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = PetsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }
            when (val result = repository.listMyPets(token)) {
                is MapsResult.Success -> _uiState.value = PetsUiState.Success(result.data)
                is MapsResult.Error -> {
                    Log.e(tag, "loadMyPets failed: ${result.message}")
                    _uiState.value = PetsUiState.Error(result.message)
                }
            }
        }
    }

    fun createPet(request: PetRequest) {
        viewModelScope.launch {
            _uiState.value = PetsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = PetsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }
            when (val result = repository.createPet(token, request)) {
                is MapsResult.Success -> _uiState.value = PetsUiState.Saved(result.data)
                is MapsResult.Error -> {
                    Log.e(tag, "createPet failed: ${result.message}")
                    _uiState.value = PetsUiState.Error(result.message)
                }
            }
        }
    }

    fun updatePet(petId: String, request: PetRequest) {
        viewModelScope.launch {
            _uiState.value = PetsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = PetsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }
            when (val result = repository.updatePet(token, petId, request)) {
                is MapsResult.Success -> _uiState.value = PetsUiState.Saved(result.data)
                is MapsResult.Error -> {
                    Log.e(tag, "updatePet failed: ${result.message}")
                    _uiState.value = PetsUiState.Error(result.message)
                }
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            _uiState.value = PetsUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = PetsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }
            when (val result = repository.deletePet(token, petId)) {
                is MapsResult.Success -> _uiState.value = PetsUiState.Deleted(petId)
                is MapsResult.Error -> {
                    Log.e(tag, "deletePet failed: ${result.message}")
                    _uiState.value = PetsUiState.Error(result.message)
                }
            }
        }
    }

    fun loadPetDetails(petId: String) {
        viewModelScope.launch {
            _selectedPet.value = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = PetsUiState.Error("Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }
            when (val result = repository.getPetDetails(token, petId)) {
                is MapsResult.Success -> _selectedPet.value = result.data
                is MapsResult.Error -> {
                    Log.e(tag, "loadPetDetails failed: ${result.message}")
                    _uiState.value = PetsUiState.Error(result.message)
                }
            }
        }
    }
}
