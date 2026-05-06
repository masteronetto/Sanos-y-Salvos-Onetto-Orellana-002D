package com.example.sanosysalvosv2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.MapRepository
import com.example.sanosysalvosv2.model.MapLayer
import com.example.sanosysalvosv2.model.NearbyReportMarker
import com.example.sanosysalvosv2.model.TileProviderConfig
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = MapRepository()

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var provider by mutableStateOf<TileProviderConfig?>(null)
        private set

    var layers by mutableStateOf<List<MapLayer>>(emptyList())
        private set

    var markers by mutableStateOf<List<NearbyReportMarker>>(emptyList())
        private set

    fun refreshMapData(
        latitude: Double = -12.0464,
        longitude: Double = -77.0428,
        radiusMeters: Int = 3000,
    ) {
        loading = true
        error = null

        viewModelScope.launch {
            try {
                provider = repository.provider()
                layers = repository.layers()
                markers = repository.nearbyReports(
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = radiusMeters,
                ).markers
            } catch (e: Exception) {
                error = e.message ?: "No se pudo cargar mapa"
            } finally {
                loading = false
            }
        }
    }
}
