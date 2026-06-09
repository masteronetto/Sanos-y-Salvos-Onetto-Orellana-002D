package com.example.sanosysalvosv2.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.R
import com.example.sanosysalvosv2.model.NearbyReport
import com.example.sanosysalvosv2.viewmodel.MapsUiState
import com.example.sanosysalvosv2.viewmodel.MapsViewModel
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val defaultLat = -33.515
private const val defaultLon = -70.757

@Composable
fun MapsScreen(viewModel: MapsViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedReport by remember { mutableStateOf<NearbyReport?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            mapView?.enableMyLocation()
            resolveLocationAndLoad(context, mapView, viewModel)
        } else {
            viewModel.onLocationUnavailable()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            mapView?.enableMyLocation()
            resolveLocationAndLoad(context, mapView, viewModel)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is MapsUiState.AwaitingLocation) {
            while (true) {
                val resolved = resolveLocationAndLoad(context, mapView, viewModel)
                if (resolved) break
                delay(1500)
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is MapsUiState.Error) {
            val message = (uiState as MapsUiState.Error).message
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    mapView = map
                    map.setTileSource(cartodarkSource())
                    map.setMultiTouchControls(true)
                    map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    map.controller.setZoom(15.5)
                    map.controller.setCenter(GeoPoint(defaultLat, defaultLon))
                    map.overlays.add(MyLocationNewOverlay(GpsMyLocationProvider(ctx), map))
                }
            },
            update = { it.invalidate() },
            modifier = Modifier.fillMaxSize(),
        )

        if (uiState is MapsUiState.Loading || uiState is MapsUiState.AwaitingLocation) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        when (uiState) {
            is MapsUiState.AwaitingLocation -> {
                InfoBanner(
                    text = "Esperando ubicacion para consultar reportes cercanos...",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 72.dp)
                        .padding(horizontal = 16.dp),
                )
            }

            is MapsUiState.Loading -> {
                InfoBanner(
                    text = "Cargando reportes cercanos...",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 72.dp)
                        .padding(horizontal = 16.dp),
                )
            }

            is MapsUiState.Success -> {
                val reports = (uiState as MapsUiState.Success).reports
                if (reports.isEmpty()) {
                    InfoBanner(
                        text = "No hay reportes cercanos en tu zona por ahora.",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 72.dp)
                            .padding(horizontal = 16.dp),
                    )
                }
            }

            is MapsUiState.Error -> {
                val message = (uiState as MapsUiState.Error).message
                InfoBanner(
                    text = message,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 72.dp)
                        .padding(horizontal = 16.dp),
                )
            }
        }

        // Info card shown when a map marker is tapped (inside Box for BoxScope.align)
        selectedReport?.let { report ->
            val isLost = report.description.startsWith("lost", ignoreCase = true)
            val statusLabel = if (isLost) "PERDIDO" else "ENCONTRADO"
            val statusColor = if (isLost) Color.Red else Color(0xFF2E7D32)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = report.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                    // TODO: show photo thumbnail once NearbyReport gains a photoUrl field.
                    // No image-loading library present -- load with Coil when added.
                    Text(
                        text = "Foto no disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                    Text(
                        text = "X Cerrar",
                        modifier = Modifier.clickable { selectedReport = null },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState, mapView) {
        if (uiState is MapsUiState.Success) {
            selectedReport = null
            mapView?.renderReports(
                reports = (uiState as MapsUiState.Success).reports,
                onMarkerSelected = { selectedReport = it },
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDetach()
        }
    }
}

fun cartodarkSource() = XYTileSource(
    "CartoDark",
    0,
    19,
    256,
    ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/dark_all/",
        "https://b.basemaps.cartocdn.com/dark_all/",
        "https://c.basemaps.cartocdn.com/dark_all/",
    ),
    "© CartoDB © OpenStreetMap contributors",
)

fun MapView.enableMyLocation() {
    val overlay = overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
        ?: MyLocationNewOverlay(GpsMyLocationProvider(context), this).also { overlays.add(it) }
    overlay.enableMyLocation()
    invalidate()
}

private fun MapView.renderReports(
    reports: List<NearbyReport>,
    onMarkerSelected: (NearbyReport?) -> Unit,
) {
    // TODO(Step C): add per-report radius Polygon once NearbyReportMarker gains a radiusMeters field.
    overlays.removeAll { it is Marker }
    reports.forEach { report ->
        val isLost = report.description.startsWith("lost", ignoreCase = true)
        val iconRes = if (isLost) R.drawable.ic_marker_lost else R.drawable.ic_marker_found
        val icon = ContextCompat.getDrawable(context, iconRes)
        Marker(this).apply {
            position = GeoPoint(report.lat, report.lon)
            title = report.title
            snippet = report.description
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon?.let { setIcon(it) }
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ ->
                onMarkerSelected(report)
                true
            }
        }.also { overlays.add(it) }
    }
    invalidate()
}

private fun updateMapCenter(mapView: MapView?, lat: Double, lon: Double) {
    mapView?.controller?.setCenter(GeoPoint(lat, lon))
}

private fun resolveLocationAndLoad(context: Context, mapView: MapView?, viewModel: MapsViewModel): Boolean {
    val location = context.lastKnownLocation()
    val lat = location?.latitude
    val lon = location?.longitude
    if (lat == null || lon == null || lat == 0.0 || lon == 0.0) {
        viewModel.onLocationUnavailable()
        return false
    }

    updateMapCenter(mapView, lat, lon)
    viewModel.onLocationAvailable(lat, lon)
    return true
}

@SuppressLint("MissingPermission")
private fun Context.lastKnownLocation(): Location? {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    return providers
        .filter { provider -> locationManager.isProviderEnabled(provider) }
        .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
        .maxByOrNull { location -> location.time }
}

@Composable
private fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}