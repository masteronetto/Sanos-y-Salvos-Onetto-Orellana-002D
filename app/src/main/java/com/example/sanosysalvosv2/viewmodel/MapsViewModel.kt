package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.MapsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.NearbyReport
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
    private val mapsRepository = MapsRepository()

    private val _uiState = MutableStateFlow<MapsUiState>(MapsUiState.AwaitingLocation)
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastRadiusMeters: Int = 3000
    private var pendingRetryWhenLocationAvailable: Boolean = true

    fun fetchNearbyReports(lat: Double, lon: Double, radiusMeters: Int = 3000) {
        if (!isValidLocation(lat, lon)) {
            pendingRetryWhenLocationAvailable = true
            _uiState.value = MapsUiState.AwaitingLocation
            Log.d("MapsViewModel", "Skip nearby reports call: invalid location lat=$lat lon=$lon")
            return
        }

        lastLat = lat
        lastLon = lon
        lastRadiusMeters = radiusMeters
        pendingRetryWhenLocationAvailable = false
        _uiState.value = MapsUiState.Loading

        viewModelScope.launch {
            val token = sessionStore.tokenFlow.first()
            if (token.isNullOrBlank()) {
                _uiState.value = MapsUiState.Error("Sesión no válida")
                return@launch
            }

            when (val result = mapsRepository.getNearbyReports(token, lat, lon, radiusMeters)) {
                is MapsResult.Success -> {
                    _uiState.value = MapsUiState.Success(result.data)
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
            fetchNearbyReports(lat, lon, lastRadiusMeters)
        }
    }

    fun onLocationUnavailable() {
        pendingRetryWhenLocationAvailable = true
        if (_uiState.value !is MapsUiState.Success) {
            _uiState.value = MapsUiState.AwaitingLocation
        }
    }

    fun refreshLastKnownLocation() {
        pendingRetryWhenLocationAvailable = true
        val lat = lastLat
        val lon = lastLon
        if (lat != null && lon != null && isValidLocation(lat, lon)) {
            fetchNearbyReports(lat, lon, lastRadiusMeters)
            return
        }
        _uiState.value = MapsUiState.AwaitingLocation
    }

    private fun isValidLocation(lat: Double?, lon: Double?): Boolean {
        return lat != null && lon != null && lat != 0.0 && lon != 0.0 && !lat.isNaN() && !lon.isNaN()
    }
}