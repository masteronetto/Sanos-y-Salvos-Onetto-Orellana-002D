package com.sanosysalvos.geo.service

import com.sanosysalvos.contracts.MapLayer
import com.sanosysalvos.contracts.NavigationLinkResponse
import com.sanosysalvos.contracts.NearbyReportMarker
import com.sanosysalvos.contracts.NearbyReportsResponse
import com.sanosysalvos.contracts.ReportType
import com.sanosysalvos.contracts.TileProviderConfig
import com.sanosysalvos.geo.config.OsmProperties
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import org.springframework.stereotype.Service

@Service
class MapFacadeService(
    private val osmProperties: OsmProperties,
) {
    private val seededMarkers = listOf(
        SeededReport("RPT-001", ReportType.LOST, -33.4489, -70.6693, "Perro perdido cerca del parque"),
        SeededReport("RPT-002", ReportType.FOUND, -33.4512, -70.6619, "Gato encontrado en avenida central"),
        SeededReport("RPT-003", ReportType.LOST, -33.4442, -70.6750, "Mascota vista en zona residencial"),
        SeededReport("RPT-004", ReportType.FOUND, -33.4551, -70.6728, "Reporte de hallazgo en feria local"),
    )

    fun tileProviderConfig(): TileProviderConfig = TileProviderConfig(
        provider = "openstreetmap",
        tileUrlTemplate = osmProperties.tileUrlTemplate,
        attribution = osmProperties.attribution,
        termsUrl = osmProperties.termsUrl,
    )

    fun defaultLayers(): List<MapLayer> = listOf(
        MapLayer("streets", "Calles", "BASE", true),
        MapLayer("paths", "Caminos", "OVERLAY", true),
        MapLayer("report-markers", "Reportes cercanos", "OVERLAY", true),
        MapLayer("navigation-preview", "Navegacion", "OVERLAY", true),
    )

    fun nearbyReports(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        reportType: ReportType?,
    ): NearbyReportsResponse {
        val markers = seededMarkers
            .map { seeded ->
                val distance = haversineMeters(latitude, longitude, seeded.latitude, seeded.longitude)
                NearbyReportMarker(
                    reportId = seeded.reportId,
                    reportType = seeded.reportType,
                    latitude = seeded.latitude,
                    longitude = seeded.longitude,
                    title = seeded.title,
                    distanceMeters = distance,
                )
            }
            .filter { marker -> marker.distanceMeters <= radiusMeters }
            .filter { marker -> reportType == null || marker.reportType == reportType }
            .sortedBy { marker -> marker.distanceMeters }

        return NearbyReportsResponse(
            centerLatitude = latitude,
            centerLongitude = longitude,
            radiusMeters = radiusMeters,
            total = markers.size,
            markers = markers,
        )
    }

    fun navigationLink(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
    ): NavigationLinkResponse {
        val url = "https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=" +
            "$fromLatitude%2C$fromLongitude%3B$toLatitude%2C$toLongitude"

        return NavigationLinkResponse(
            provider = "openstreetmap",
            fromLatitude = fromLatitude,
            fromLongitude = fromLongitude,
            toLatitude = toLatitude,
            toLongitude = toLongitude,
            navigationUrl = url,
        )
    }

    private fun haversineMeters(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double,
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(latitudeA)
        val lon1 = Math.toRadians(longitudeA)
        val lat2 = Math.toRadians(latitudeB)
        val lon2 = Math.toRadians(longitudeB)

        val latitudeDiff = lat2 - lat1
        val longitudeDiff = lon2 - lon1

        val haversine = sin(latitudeDiff / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(longitudeDiff / 2).pow(2)

        return 2 * earthRadiusMeters * asin(sqrt(haversine))
    }
}

private data class SeededReport(
    val reportId: String,
    val reportType: ReportType,
    val latitude: Double,
    val longitude: Double,
    val title: String,
)
