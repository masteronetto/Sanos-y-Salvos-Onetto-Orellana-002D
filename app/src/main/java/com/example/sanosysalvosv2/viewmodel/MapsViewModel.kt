package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.api.CollaboratorsApi
import com.example.sanosysalvosv2.data.api.CollaboratorsRetrofitClient
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.UserReportsRepository
import com.example.sanosysalvosv2.data.repository.CollaboratorsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.CollaboratorResponse
import com.example.sanosysalvosv2.model.NearbyReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MapsUiState {
    data object Loading : MapsUiState()
    data object AwaitingLocation : MapsUiState()
    data class Success(
        val reports: List<NearbyReport>,
        val lostCount: Int = 0,
        val foundCount: Int = 0
    ) : MapsUiState()
    data class Error(val message: String) : MapsUiState()
}

class MapsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val sessionStore = SessionStore(application.applicationContext)
    private val userReportsRepository = UserReportsRepository()
    private val collaboratorsRepository = CollaboratorsRepository()

    private val _uiState = MutableStateFlow<MapsUiState>(MapsUiState.AwaitingLocation)
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastRadiusMeters: Int = 3000
    private var lastType: String? = null
    private var pendingRetryWhenLocationAvailable: Boolean = true

    private val _collaborators = MutableStateFlow<List<CollaboratorResponse>>(emptyList())
    val collaborators: StateFlow<List<CollaboratorResponse>> = _collaborators.asStateFlow()

    val comunaCoordinates = mapOf(
        "Maipú" to Pair(-33.5132, -70.7653),
        "Maipu" to Pair(-33.5132, -70.7653),
        "Providencia" to Pair(-33.4322, -70.6108),
        "Ñuñoa" to Pair(-33.4569, -70.5983),
        "Nunoa" to Pair(-33.4569, -70.5983),
        "Las Condes" to Pair(-33.4163, -70.5956),
        "La Florida" to Pair(-33.5235, -70.5912),
        "Santiago" to Pair(-33.4489, -70.6693),
        "Cerrillos" to Pair(-33.4951, -70.7234),
        "Pucón" to Pair(-39.2722, -71.9797),
        "Pucon" to Pair(-39.2722, -71.9797),
        "Vitacura" to Pair(-33.3833, -70.5833),
        "Peñalolén" to Pair(-33.4833, -70.5333),
        "San Miguel" to Pair(-33.4971, -70.6531),
        "Macul" to Pair(-33.4833, -70.5833),
        "Independencia" to Pair(-33.4167, -70.6667)
    )

    init {
        loadCollaborators()
    }

    fun fetchNearbyReports(lat: Double, lon: Double, radiusMeters: Int = 3000, type: String? = null) {
        if (!isValidLocation(lat, lon)) {
            pendingRetryWhenLocationAvailable = true
            _uiState.value = MapsUiState.AwaitingLocation
            return
        }

        lastLat = lat
        lastLon = lon
        lastRadiusMeters = radiusMeters
        lastType = type
        pendingRetryWhenLocationAvailable = false
        _uiState.value = MapsUiState.Loading

        viewModelScope.launch {
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = MapsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = userReportsRepository.searchNearbyReports(token, lat, lon, radiusMeters, type)) {
                is MapsResult.Success -> {
                    val nearby = result.data.mapNotNull { report ->
                        val latitude = report.latitude ?: return@mapNotNull null
                        val longitude = report.longitude ?: return@mapNotNull null
                        NearbyReport(
                            lat = latitude,
                            lon = longitude,
                            title = report.locationName ?: (report.description ?: "Reporte"),
                            description = report.description ?: "",
                            status = report.type ?: "",
                            photoUrl = report.photoUrl,
                            photoBase64 = report.photoBase64 ?: "",
                            reporterName = report.reporterName ?: report.reporterId,
                            reporterPhone = report.reporterPhone,
                            reportId = report.id,
                        )
                    }
                    _uiState.value = MapsUiState.Success(
                        reports = nearby,
                        lostCount = nearby.count { it.status.uppercase() == "LOST" },
                        foundCount = nearby.count { it.status.uppercase() == "FOUND" }
                    )
                }
                is MapsResult.Error -> {
                    Log.e("MapsViewModel", "Failed to load nearby reports: ${result.message}")
                    _uiState.value = MapsUiState.Error(result.message)
                }
            }
        }
    }

    fun onLocationAvailable(lat: Double, lon: Double) {
        if (!isValidLocation(lat, lon)) {
            onLocationUnavailable()
            return
        }

        lastLat = lat
        lastLon = lon

        val shouldRetry = pendingRetryWhenLocationAvailable ||
            _uiState.value is MapsUiState.AwaitingLocation ||
            (_uiState.value is MapsUiState.Success && (_uiState.value as MapsUiState.Success).reports.isEmpty())

        if (shouldRetry) {
            fetchNearbyReports(lat, lon, lastRadiusMeters, lastType)
        }
    }

    fun refreshLastKnownLocation() {
        pendingRetryWhenLocationAvailable = true
        val lat = lastLat
        val lon = lastLon
        if (lat != null && lon != null && isValidLocation(lat, lon)) {
            fetchNearbyReports(lat, lon, lastRadiusMeters, lastType)
            return
        }
        _uiState.value = MapsUiState.AwaitingLocation
    }

    fun refreshReportsWithType(type: String?) {
        lastType = type
        refreshReports()
    }

    fun refreshReports() {
        val lat = lastLat
        val lon = lastLon
        if (lat != null && lon != null && isValidLocation(lat, lon)) {
            fetchNearbyReports(lat, lon, lastRadiusMeters, lastType)
        }
    }

    fun onLocationUnavailable() {
        pendingRetryWhenLocationAvailable = true
        if (_uiState.value !is MapsUiState.Success) {
            _uiState.value = MapsUiState.AwaitingLocation
        }
    }

    fun loadCollaborators() {
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) return@launch

                val collaborators = collaboratorsRepository.listCollaborators(token)
                val activeCollabs = collaborators.filter { it.status.equals("ACTIVE", ignoreCase = true) }
                _collaborators.value = activeCollabs
                Log.d("MapsVM", "Loaded ${activeCollabs.size} collaborators")
            } catch (e: Exception) {
                Log.e("MapsVM", "Error loading collaborators: ${e.message}")
            }
        }
    }

    private fun isValidLocation(lat: Double?, lon: Double?): Boolean {
        return lat != null && lon != null && lat != 0.0 && lon != 0.0 && !lat.isNaN() && !lon.isNaN()
    }
}
