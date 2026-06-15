package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.UserMatchesRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.MatchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MatchesUiState {
    object Idle : MatchesUiState()
    object Loading : MatchesUiState()
    data class Success(val matches: List<MatchResponse>) : MatchesUiState()
    data class Error(val message: String) : MatchesUiState()
    object ActionDone : MatchesUiState()
}

class UserMatchesViewModel(
    application: Application,
    private val repository: UserMatchesRepository = UserMatchesRepository()
) : AndroidViewModel(application) {

    private val tag = "UserMatchesVM"
    private val sessionStore = SessionStore(application.applicationContext)

    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Idle)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    private var _allMatches = listOf<MatchResponse>()
    var activeFilter: String? = null
        private set

    fun loadMyMatches() {
        viewModelScope.launch {
            _uiState.value = MatchesUiState.Loading
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = MatchesUiState.Error("Sesión no válida")
                return@launch
            }
            when (val result = repository.getMyMatches(token)) {
                is MapsResult.Success -> {
                    _allMatches = result.data
                    applyFilter()
                }
                is MapsResult.Error -> {
                    Log.e(tag, "loadMyMatches failed: ${result.message}")
                    _uiState.value = MatchesUiState.Error(result.message)
                }
            }
        }
    }

    fun setFilter(status: String?) {
        activeFilter = status
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (activeFilter == null) _allMatches else _allMatches.filter { it.status == activeFilter }
        _uiState.value = MatchesUiState.Success(filtered)
    }

    fun acceptMatch(matchId: String) {
        viewModelScope.launch {
            val token = sessionStore.tokenFlow.first() ?: return@launch
            when (repository.acceptMatch(token, matchId)) {
                is MapsResult.Success -> {
                    _uiState.value = MatchesUiState.ActionDone
                    loadMyMatches()
                }
                is MapsResult.Error -> _uiState.value = MatchesUiState.Error("No se pudo confirmar")
            }
        }
    }

    fun rejectMatch(matchId: String) {
        viewModelScope.launch {
            val token = sessionStore.tokenFlow.first() ?: return@launch
            when (repository.rejectMatch(token, matchId)) {
                is MapsResult.Success -> {
                    _uiState.value = MatchesUiState.ActionDone
                    loadMyMatches()
                }
                is MapsResult.Error -> _uiState.value = MatchesUiState.Error("No se pudo rechazar")
            }
        }
    }

    private val _selectedMatch = MutableStateFlow<MatchResponse?>(null)
    val selectedMatch: StateFlow<MatchResponse?> = _selectedMatch.asStateFlow()

    fun loadMatchDetails(matchId: String) {
        viewModelScope.launch {
            _selectedMatch.value = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = MatchesUiState.Error("Sesión no válida")
                return@launch
            }
            when (val result = repository.getMatchDetails(token, matchId)) {
                is MapsResult.Success -> _selectedMatch.value = result.data
                is MapsResult.Error -> {
                    Log.e(tag, "loadMatchDetails failed: ${result.message}")
                    _uiState.value = MatchesUiState.Error(result.message)
                }
            }
        }
    }
}
