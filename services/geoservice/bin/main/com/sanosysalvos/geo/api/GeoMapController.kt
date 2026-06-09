package com.sanosysalvos.geo.api

import com.sanosysalvos.contracts.MapLayer
import com.sanosysalvos.contracts.NearbyReportMarker
import com.sanosysalvos.contracts.NearbyReportsResponse
import com.sanosysalvos.contracts.NavigationLinkResponse
import com.sanosysalvos.contracts.ReportType
import com.sanosysalvos.contracts.TileProviderConfig
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/geo/map")
class GeoMapController {

    @GetMapping("/provider")
    fun provider(): TileProviderConfig = TileProviderConfig(
        provider = "openstreetmap",
        tileUrlTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        attribution = "© OpenStreetMap contributors",
        termsUrl = "https://www.openstreetmap.org/copyright",
    )

    @GetMapping("/layers")
    fun layers(): List<MapLayer> = listOf(
        MapLayer(id = "lost", displayName = "Mascotas perdidas", type = "marker", enabledByDefault = true),
        MapLayer(id = "found", displayName = "Mascotas encontradas", type = "marker", enabledByDefault = true),
        MapLayer(id = "alerts", displayName = "Alertas comunitarias", type = "heatmap", enabledByDefault = false),
    )

    @GetMapping("/reports/nearby")
    fun nearbyReports(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "3000") radiusMeters: Int,
        @RequestParam(required = false) reportType: ReportType?,
    ): NearbyReportsResponse {
        val sample = listOf(
            NearbyReportMarker(
                reportId = "RPT-1001",
                reportType = ReportType.LOST,
                latitude = latitude + 0.0042,
                longitude = longitude + 0.0031,
                title = "Perrito marron con collar azul",
                distanceMeters = distanceMeters(latitude, longitude, latitude + 0.0042, longitude + 0.0031),
            ),
            NearbyReportMarker(
                reportId = "RPT-1002",
                reportType = ReportType.FOUND,
                latitude = latitude - 0.0036,
                longitude = longitude + 0.0017,
                title = "Gatito blanco visto en parque",
                distanceMeters = distanceMeters(latitude, longitude, latitude - 0.0036, longitude + 0.0017),
            ),
            NearbyReportMarker(
                reportId = "RPT-1003",
                reportType = ReportType.LOST,
                latitude = latitude + 0.0012,
                longitude = longitude - 0.0050,
                title = "Aviso comunitario: posible avistamiento",
                distanceMeters = distanceMeters(latitude, longitude, latitude + 0.0012, longitude - 0.0050),
            ),
        )

        val filtered = sample
            .filter { it.distanceMeters <= radiusMeters }
            .filter { reportType == null || it.reportType == reportType }

        return NearbyReportsResponse(
            centerLatitude = latitude,
            centerLongitude = longitude,
            radiusMeters = radiusMeters,
            total = filtered.size,
            markers = filtered,
        )
    }

    @GetMapping("/navigation/link")
    fun navigationLink(
        @RequestParam fromLatitude: Double,
        @RequestParam fromLongitude: Double,
        @RequestParam toLatitude: Double,
        @RequestParam toLongitude: Double,
    ): NavigationLinkResponse = NavigationLinkResponse(
        provider = "google-maps",
        fromLatitude = fromLatitude,
        fromLongitude = fromLongitude,
        toLatitude = toLatitude,
        toLongitude = toLongitude,
        navigationUrl = "https://www.google.com/maps/dir/?api=1&origin=$fromLatitude,$fromLongitude&destination=$toLatitude,$toLongitude&travelmode=driving",
    )

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
