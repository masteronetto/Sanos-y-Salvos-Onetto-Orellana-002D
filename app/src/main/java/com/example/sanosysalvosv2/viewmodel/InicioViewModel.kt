package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.PetRepository
import com.example.sanosysalvosv2.data.repository.PetsRepository
import com.example.sanosysalvosv2.data.repository.ProfileRepository
import com.example.sanosysalvosv2.data.repository.UserMatchesRepository
import com.example.sanosysalvosv2.data.repository.UserReportsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.ReportTypeMapper
import com.example.sanosysalvosv2.model.ReportTypes
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class NotificationType {
    MATCH, REPORT, SYSTEM
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
    data object Loading : InicioUiState()
    data class Success(val summary: InicioSummary) : InicioUiState()
    data class Error(val message: String) : InicioUiState()
}

class InicioViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "InicioViewModel"
    private val sessionStore = SessionStore(application.applicationContext)
    private val profileRepository = ProfileRepository()
    private val petsRepository = PetsRepository()
    private val userReportsRepository = UserReportsRepository()
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
                // Fetch only the current user's reports for dashboard counts
                val profileDeferred = async { profileRepository.getMe(token) }
                val matchesDeferred = async { matchRepository.getMyMatches(token) }
                val petsDeferred = async { petsRepository.listMyPets(token) }
                val reportsDeferred = async { userReportsRepository.listMyReports(token) }

                val profileResult = profileDeferred.await()
                val petsResult = petsDeferred.await()
                val matchesResult = matchesDeferred.await()
                val reportsResult = reportsDeferred.await()

                if (profileResult is MapsResult.Error) {
                    _uiState.value = InicioUiState.Error(profileResult.message)
                    return@launch
                }

                val profile = (profileResult as MapsResult.Success).data
                val pets = if (petsResult is MapsResult.Success) petsResult.data else emptyList()
                val matches = if (matchesResult is MapsResult.Success) matchesResult.data else emptyList()
                val reports = if (reportsResult is MapsResult.Success) reportsResult.data else emptyList()

                // Calculate counts from ALL reports (Global)
                val lostCount = reports.count { ReportTypeMapper.normalizeType(it.type) == ReportTypes.LOST }
                val foundCount = reports.count { ReportTypeMapper.normalizeType(it.type) == ReportTypes.FOUND }

                val summary = InicioSummary(
                    userName = profile.fullName.ifBlank { "Usuario" },
                    myPetsCount = pets.size,
                    lostCount = lostCount,
                    foundCount = foundCount,
                    matchesCount = matches.size,
                    recentNotifications = matches
                        .filter { it.status == "PENDING" }
                        .sortedByDescending { it.createdAt ?: 0L }
                        .take(2)
                        .map { match ->
                            NotificationItem(
                                title = "Nueva coincidencia",
                                subtitle = "¡Tenemos noticias sobre un reporte!",
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
        if (timestamp == 0L) return "Recientemente"
        val diff = System.currentTimeMillis() - timestamp
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            else -> "Hace $days d"
        }
    }
}
