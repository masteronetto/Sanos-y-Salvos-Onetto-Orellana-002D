package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.UserCoincidenciasRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.UserMatchResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserCoincidenciasViewModel(
    application: Application,
    private val repository: UserCoincidenciasRepository = UserCoincidenciasRepository(),
) : AndroidViewModel(application) {

    private val tag = "UserCoincidenciasVM"
    private val sessionStore = SessionStore(application.applicationContext)

    var matches by mutableStateOf<List<UserMatchResponse>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var currentPage = 1
        private set
    var hasMore = true
        private set

    fun loadMatches(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                currentPage = 1
                hasMore = true
                matches = emptyList()
                error = null
            }

            if (!hasMore) return@launch

            loading = true
            error = null

            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                loading = false
                error = "Sesión no válida"
                return@launch
            }

            when (val result = repository.listMyMatches(token, page = currentPage, perPage = 20)) {
                is MapsResult.Success -> {
                    val loaded = result.data
                    if (refresh) {
                        matches = loaded
                    } else {
                        matches = matches + loaded
                    }
                    hasMore = loaded.size >= 20
                    if (loaded.isNotEmpty()) {
                        currentPage++
                    }
                    loading = false
                }
                is MapsResult.Error -> {
                    Log.e(tag, "loadMatches failed: ${result.message}")
                    error = result.message
                    loading = false
                }
            }
        }
    }

    fun acceptMatch(id: String) {
        viewModelScope.launch {
            error = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                error = "Sesión no válida"
                return@launch
            }
            when (val result = repository.acceptMatch(token, id)) {
                is MapsResult.Success -> loadMatches(refresh = true)
                is MapsResult.Error -> error = result.message
            }
        }
    }

    fun rejectMatch(id: String) {
        viewModelScope.launch {
            error = null
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                error = "Sesión no válida"
                return@launch
            }
            when (val result = repository.rejectMatch(token, id)) {
                is MapsResult.Success -> loadMatches(refresh = true)
                is MapsResult.Error -> error = result.message
            }
        }
    }
}
