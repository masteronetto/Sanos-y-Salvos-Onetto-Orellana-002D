package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.ProfileRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.UpdateProfileRequest
import com.example.sanosysalvosv2.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    object Saving : ProfileUiState()
    object Saved : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "ProfileViewModel"

    private val sessionStore = SessionStore(application.applicationContext)
    private val repository = ProfileRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private var currentUserId: String = ""

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = ProfileUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.getMe(token)) {
                is MapsResult.Success -> {
                    currentUserId = result.data.id
                    _uiState.value = ProfileUiState.Success(result.data)
                }
                is MapsResult.Error -> {
                    Log.e(tag, "loadProfile failed: ${result.message}")
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }

    fun updateProfile(fullName: String, phone: String, city: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Saving
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank() || currentUserId.isBlank()) {
                _uiState.value = ProfileUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.updateProfile(token, currentUserId, UpdateProfileRequest(fullName.trim(), phone.trim(), city.trim()))) {
                is MapsResult.Success -> {
                    _uiState.value = ProfileUiState.Saved
                    loadProfile()
                }
                is MapsResult.Error -> {
                    Log.e(tag, "updateProfile failed: ${result.message}")
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = ProfileUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = repository.logout(token)) {
                is MapsResult.Success -> {
                    sessionStore.clearSession()
                    _isLoggedOut.value = true
                }
                is MapsResult.Error -> {
                    Log.e(tag, "logout failed: ${result.message}")
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }
}
