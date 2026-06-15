package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.PetsRepository
import com.example.sanosysalvosv2.data.repository.ProfileRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.data.repository.UserMatchesRepository
import com.example.sanosysalvosv2.model.MatchResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class NotificationType {
    MATCH,
    REPORT,
    SYSTEM,
}

data class NotificationItem(
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val type: NotificationType,
)

data class InicioSummary(
    val userName: String,
    val myPetsCount: Int,
    val lostCount: Int,
    val foundCount: Int,
    val matchesCount: Int,
    val recentNotifications: List<NotificationItem>,
)

sealed class InicioUiState {
    object Loading : InicioUiState()
    data class Success(val summary: InicioSummary) : InicioUiState()
    data class Error(val message: String) : InicioUiState()
}

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "InicioViewModel"
    private val sessionStore = SessionStore(application.applicationContext)
    private val profileRepository = ProfileRepository()
    private val petsRepository = PetsRepository()
    private val matchRepository = UserMatchesRepository()

    private val _uiState = MutableStateFlow<InicioUiState>(InicioUiState.Loading)
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun refresh() {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = InicioUiState.Loading

            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = InicioUiState.Error("Sesión no válida")
                return@launch
            }

            try {
                val profileDeferred = async { profileRepository.getMe(token) }
                val matchesDeferred = async { matchRepository.getMyMatches(token) }
                val petsDeferred = async { petsRepository.listMyPets(token) }

                val profileResult = profileDeferred.await()
                if (profileResult is MapsResult.Error) {
                    _uiState.value = InicioUiState.Error(profileResult.message)
                    return@launch
                }

                val petsResult = petsDeferred.await()
                if (petsResult is MapsResult.Error) {
                    _uiState.value = InicioUiState.Error(petsResult.message)
                    return@launch
                }

                val matchesResult = matchesDeferred.await()
                if (matchesResult is MapsResult.Error) {
                    _uiState.value = InicioUiState.Error(matchesResult.message)
                    return@launch
                }

                val profile = (profileResult as MapsResult.Success).data
                val pets = (petsResult as MapsResult.Success).data
                val matches = (matchesResult as MapsResult.Success).data

                val summary = InicioSummary(
                    userName = profile.fullName.ifBlank { "Usuario" },
                    myPetsCount = pets.size,
                    lostCount = 0,
                    foundCount = 0,
                    matchesCount = matches.size,
                    recentNotifications = matches
                        .filter { it.status == "PENDING" }
                        .sortedByDescending { it.createdAt ?: 0L }
                        .take(2)
                        .map { match ->
                            NotificationItem(
                                title = "Nueva coincidencia",
                                subtitle = "Coincidencia ${match.id}",
                                timeAgo = formatTimeAgo(match.createdAt ?: 0L),
                                type = NotificationType.MATCH,
                            )
                        },
                )
                _uiState.value = InicioUiState.Success(summary)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load inicio summary", e)
                _uiState.value = InicioUiState.Error(e.message ?: "Error inesperado al cargar resumen")
            }
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val epochMillis = if (timestamp > 9_999_999_999L) timestamp else timestamp * 1000
        val nowMillis = System.currentTimeMillis()
        val diff = (nowMillis - epochMillis).coerceAtLeast(0L)

        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            days > 0 -> "Hace $days día${if (days > 1) "s" else ""}"
            hours > 0 -> "Hace $hours hora${if (hours > 1) "s" else ""}"
            minutes > 0 -> "Hace $minutes minuto${if (minutes > 1) "s" else ""}"
            else -> "Hace unos segundos"
        }
    }
}
