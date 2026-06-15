package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.api.MatchResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.repository.MatchRepository
import com.example.sanosysalvosv2.repository.MatchResult_ as RepoMatchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MatchUiState {
    object Idle : MatchUiState()
    object Loading : MatchUiState()
    data class MatchesFound(val matches: List<MatchResult>) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val matchRepository = MatchRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Idle)
    val uiState: StateFlow<MatchUiState> = _uiState

    fun checkForMatches() {
        viewModelScope.launch {
            try {
                _uiState.value = MatchUiState.Loading
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _uiState.value = MatchUiState.Idle
                    return@launch
                }

                when (val result = matchRepository.getMyMatches(token)) {
                    is RepoMatchResult.Success -> {
                        val matches = result.data
                        if (matches.isNotEmpty()) _uiState.value = MatchUiState.MatchesFound(matches) else _uiState.value = MatchUiState.Idle
                    }
                    is RepoMatchResult.Error -> {
                        _uiState.value = MatchUiState.Error(result.exception.message ?: "Error")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MatchUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun dismissMatches() {
        _uiState.value = MatchUiState.Idle
    }
}
