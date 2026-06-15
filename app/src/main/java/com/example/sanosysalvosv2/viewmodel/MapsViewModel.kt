package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.UserReportsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.NearbyReport
import com.example.sanosysalvosv2.data.repository.CollaboratorsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MapsUiState {
    data object Loading : MapsUiState()
    data object AwaitingLocation : MapsUiState()
    data class Success(val reports: List<NearbyReport>) : MapsUiState()
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

    // Collaborator markers exposed to UI
    data class CollaboratorMarker(
        val id: String,
        val name: String,
        val type: String,
        val comuna: String,
        val lat: Double,
        val lng: Double,
    )

    private val _collaborators = MutableStateFlow<List<CollaboratorMarker>>(emptyList())
    val collaborators: StateFlow<List<CollaboratorMarker>> = _collaborators.asStateFlow()

    private val comunaCoordinates = mapOf(
        "Maipú" to Pair(-33.5132, -70.7653),
        "Providencia" to Pair(-33.4372, -70.6108),
        "Ñuñoa" to Pair(-33.4569, -70.5983),
        "Las Condes" to Pair(-33.4163, -70.5956),
        "La Florida" to Pair(-33.5235, -70.5912),
        "Santiago" to Pair(-33.4489, -70.6693),
        "Cerrillos" to Pair(-33.4951, -70.7234),
        "Puente Alto" to Pair(-33.6105, -70.5688),
        "La Reina" to Pair(-33.4208, -70.5283),
        "Macul" to Pair(-33.5009, -70.5583),
        "Vitacura" to Pair(-33.3968, -70.5708),
        "Lo Barnechea" to Pair(-33.3663, -70.5358),
        "Peñalolén" to Pair(-33.4947, -70.5458),
        "La Granja" to Pair(-33.5805, -70.6383),
        "Coyoacán" to Pair(-33.5595, -70.6695),
        "Quilicura" to Pair(-33.3905, -70.7508),
        "Quinta Normal" to Pair(-33.4538, -70.7088),
        "Estación Central" to Pair(-33.4663, -70.6958),
        "Recoleta" to Pair(-33.4107, -70.6358),
        "Renca" to Pair(-33.3855, -70.7283),
        "Huechuraba" to Pair(-33.3505, -70.6908),
        "Conchalí" to Pair(-33.3805, -70.6558),
        "San Joaquín" to Pair(-33.5473, -70.6295),
        "San Miguel" to Pair(-33.4751, -70.6483),
        "La Cisterna" to Pair(-33.5280, -70.6783),
        "San Ramón" to Pair(-33.5632, -70.6695),
        "La Pintana" to Pair(-33.6190, -70.6995),
        "Pucón" to Pair(-39.2722, -71.9797),
    )

    fun fetchNearbyReports(lat: Double, lon: Double, radiusMeters: Int = 3000, type: String? = null) {
        if (!isValidLocation(lat, lon)) {
            pendingRetryWhenLocationAvailable = true
            _uiState.value = MapsUiState.AwaitingLocation
            Log.d("MapsViewModel", "Skip nearby reports call: invalid location lat=$lat lon=$lon")
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
                            reporterName = report.reporterId,
                            reportId = report.id,
                        )
                    }
                    _uiState.value = MapsUiState.Success(nearby)
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
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) return@launch
            try {
                val list = withContext(Dispatchers.IO) {
                    collaboratorsRepository.listCollaborators(token)
                }
                val markers = list.map { collab ->
                    val coords = comunaCoordinates[collab.comuna] ?: Pair(-33.4489, -70.6693)
                    CollaboratorMarker(
                        id = collab.id,
                        name = collab.name,
                        type = collab.type,
                        comuna = collab.comuna,
                        lat = coords.first,
                        lng = coords.second,
                    )
                }
                _collaborators.value = markers
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error loading collaborators: ${e.message}")
            }
        }
    }

    private fun isValidLocation(lat: Double?, lon: Double?): Boolean {
        return lat != null && lon != null && lat != 0.0 && lon != 0.0 && !lat.isNaN() && !lon.isNaN()
    }
}