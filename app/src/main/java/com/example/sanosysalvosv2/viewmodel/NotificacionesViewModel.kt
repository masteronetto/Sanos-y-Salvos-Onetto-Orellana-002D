package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.api.MatchesRetrofitClient
import com.example.sanosysalvosv2.data.api.UserMatchesApi
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.MatchResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class NotificacionesUiState {
    object Loading : NotificacionesUiState()
    object Empty : NotificacionesUiState()
    data class Success(
        val notifications: List<NotificationListItem>
    ) : NotificacionesUiState()
    data class Error(val message: String) : NotificacionesUiState()
}

data class NotificationListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val type: String,
    val isRead: Boolean = false,
)

class NotificacionesViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val sessionStore = SessionStore(application.applicationContext)
    private val _uiState = MutableStateFlow<NotificacionesUiState>(NotificacionesUiState.Loading)
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    private var allNotifications = listOf<NotificationListItem>()
    var activeFilter by mutableStateOf("Todas")
        private set

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificacionesUiState.Loading
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _uiState.value = NotificacionesUiState.Error("Sesión no válida")
                    return@launch
                }

                val response = MatchesRetrofitClient.retrofit
                    .create(UserMatchesApi::class.java)
                    .getMyMatches("Bearer $token")

                if (response.isSuccessful) {
                    val matches = parseMatchesJson(response.body())

                    allNotifications = matches.map { match ->
                        val title = when (match.status.uppercase()) {
                            "PENDING" -> "Nueva coincidencia encontrada"
                            "CONFIRMED" -> "Coincidencia confirmada"
                            "REJECTED" -> "Coincidencia rechazada"
                            else -> "Actualización de coincidencia"
                        }
                        val subtitle = match.reason.ifEmpty {
                            "Score de coincidencia: ${match.score}%"
                        }
                        NotificationListItem(
                            id = match.id,
                            title = title,
                            subtitle = subtitle,
                            timeAgo = formatTimeAgo(match.createdAt ?: 0L),
                            type = "match"
                        )
                    }

                    _uiState.value = if (allNotifications.isEmpty()) {
                        NotificacionesUiState.Empty
                    } else {
                        NotificacionesUiState.Success(allNotifications)
                    }
                } else {
                    _uiState.value = NotificacionesUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = NotificacionesUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun setFilter(filter: String) {
        activeFilter = filter
        val filtered = when (filter) {
            "Coincidencias" -> allNotifications.filter { it.type == "match" }
            "Reportes" -> allNotifications.filter { it.type == "report" }
            "Sistema" -> allNotifications.filter { it.type == "system" }
            else -> allNotifications
        }
        _uiState.value = if (filtered.isEmpty()) {
            NotificacionesUiState.Empty
        } else {
            NotificacionesUiState.Success(filtered)
        }
    }

    private fun parseMatchesJson(body: JsonElement?): List<MatchResponse> {
        if (body == null || body.isJsonNull) return emptyList()

        return try {
            val gson = Gson()
            if (body.isJsonArray) {
                gson.fromJson(body, object : TypeToken<List<MatchResponse>>() {}.type)
            } else {
                val obj = body.asJsonObject
                val candidateKeys = listOf("data", "results", "items", "matches", "list")
                for (key in candidateKeys) {
                    if (obj.has(key)) {
                        val element = obj.get(key)
                        if (element.isJsonArray) {
                            return gson.fromJson(element, object : TypeToken<List<MatchResponse>>() {}.type)
                        }
                    }
                }

                listOf(gson.fromJson(body, MatchResponse::class.java))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Hace un momento"
            diff < 3_600_000 -> "Hace ${diff / 60_000} minutos"
            diff < 86_400_000 -> "Hace ${diff / 3_600_000} horas"
            diff < 604_800_000 -> "Hace ${diff / 86_400_000} días"
            else -> "Hace más de una semana"
        }
    }
}
