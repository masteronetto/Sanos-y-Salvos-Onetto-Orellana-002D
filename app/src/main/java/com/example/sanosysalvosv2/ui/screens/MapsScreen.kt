package com.example.sanosysalvosv2.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.compose.foundation.layout.width
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.R
import com.example.sanosysalvosv2.model.NearbyReport
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.viewmodel.MapsUiState
import com.example.sanosysalvosv2.viewmodel.MapsViewModel
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val defaultLat = -33.515
private const val defaultLon = -70.757

private enum class MapLegendCategory(
    val marker: String,
    val label: String,
    val color: Color,
    val isCollaborator: Boolean = false,
) {
    Lost("P", "Perdidas", Color(0xFFE53935)),
    Found("E", "Encontradas", Color(0xFF4A9B8E)),
    Veterinarias("V", "Veterinarias", Color(0xFF4A9B8E), isCollaborator = true),
    Refugios("R", "Refugios", Color(0xFFFF8C00), isCollaborator = true),
    Municipios("M", "Municipios", Color(0xFF1976D2), isCollaborator = true),
}

@Composable
fun MapsScreen(
    viewModel: MapsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val collaborators by viewModel.collaborators.collectAsState()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedReport by remember { mutableStateOf<NearbyReport?>(null) }
    var selectedCollaborator by remember { mutableStateOf<com.example.sanosysalvosv2.viewmodel.MapsViewModel.CollaboratorMarker?>(null) }
    var selectedCategories by remember {
        mutableStateOf(
            setOf(
                MapLegendCategory.Lost,
                MapLegendCategory.Found,
                MapLegendCategory.Veterinarias,
                MapLegendCategory.Refugios,
                MapLegendCategory.Municipios,
            ),
        )
    }
    val showReports = selectedCategories.any { !it.isCollaborator }
    val showCollaborators = selectedCategories.any { it.isCollaborator }

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
        // load collaborators once
        viewModel.loadCollaborators()
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

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    mapView = map
                    map.setTileSource(TileSourceFactory.MAPNIK)
                    map.setBackgroundColor(android.graphics.Color.WHITE)
                    map.setMultiTouchControls(true)
                    map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(GeoPoint(defaultLat, defaultLon))
                    map.overlays.add(
                        MyLocationNewOverlay(GpsMyLocationProvider(ctx), map).apply {
                            AppCompatResources.getDrawable(ctx, R.drawable.ic_my_location_marker)
                                ?.let { drawable ->
                                    (drawable as? BitmapDrawable)?.bitmap?.let { bmp ->
                                        setPersonIcon(bmp)
                                    }
                                }
                            setPersonAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                    )
                }
            },
            update = { map ->
                map.invalidate()
                map.setOnTouchListener { _, _ ->
                    if (selectedReport != null) selectedReport = null
                    false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
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
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp),
                )
            }

            is MapsUiState.Loading -> {
                InfoBanner(
                    text = "Cargando reportes cercanos...",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
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
                            .padding(top = 8.dp)
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
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp),
                )
            }
        }

        if (selectedReport == null) {
            MapLegend(
                selectedCategories = selectedCategories,
                onCategoryToggle = { category ->
                    val newCategories = if (selectedCategories.contains(category)) {
                        selectedCategories - category
                    } else {
                        selectedCategories + category
                    }
                    selectedCategories = newCategories
                    if (!category.isCollaborator) {
                        viewModel.refreshReportsWithType(deriveReportType(newCategories))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp),
            )
        }

        // Center-on-user floating action button
        FloatingActionButton(
            onClick = { resolveLocationAndLoad(context, mapView, viewModel) },
            containerColor = Color(0xFF4A9B8E),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Mi ubicación",
            )
        }

        // Info card shown when a map marker is tapped (inside Box for BoxScope.align)
        selectedReport?.let { report ->
            val isLost = report.status.equals("lost", ignoreCase = true) || report.description.startsWith("lost", ignoreCase = true)
            val statusLabel = if (isLost) "PERDIDO" else "ENCONTRADO"
            val statusColor = if (isLost) Color(0xFFC62828) else Color(0xFF4A9B8E)

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.94f)
                    .padding(16.dp),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFB2DFDB)),
                shadowElevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.reporterName?.let { "Reportado por $it" } ?: "Reportero desconocido",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                            )
                        }

                        IconButton(onClick = { selectedReport = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = report.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    report.photoUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Foto del reporte",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFB2DFDB)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pets,
                            contentDescription = "Foto no disponible",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp),
                        )
                    }

                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Collaborator info card (separate from report card)
        selectedCollaborator?.let { collab ->
            val typeLabel = when {
                collab.type.uppercase().contains("VETER") || collab.type.uppercase().contains("CLINIC") -> "Clínica"
                collab.type.uppercase().contains("SHELTER") || collab.type.uppercase().contains("REFUGIO") -> "Refugio"
                collab.type.uppercase().contains("MUNIC") -> "Municipalidad"
                else -> collab.type
            }
            val badgeColor = when (typeLabel) {
                "Clínica" -> Color(0xFF4A9B8E)
                "Refugio" -> Color(0xFFFF8C00)
                "Municipalidad" -> Color(0xFF1976D2)
                else -> Color(0xFF4A9B8E)
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.94f)
                    .padding(16.dp),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFB2DFDB)),
                shadowElevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = collab.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = collab.comuna,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                            )
                        }
                        IconButton(onClick = { selectedCollaborator = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = typeLabel,
                            color = badgeColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = collab.comuna,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Button(
                        onClick = { /* TODO: Navigate to collaborator detail screen */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    ) {
                        Text("Ver detalles", color = Color.White)
                    }
                }
            }
        }
    } // end Box
    } // end Column

    LaunchedEffect(uiState, mapView, collaborators, selectedCategories) {
        if (uiState is MapsUiState.Success) {
            selectedReport = null
            selectedCollaborator = null
            mapView?.renderReports(
                reports = (uiState as MapsUiState.Success).reports,
                collaborators = collaborators,
                selectedCategories = selectedCategories,
                showReports = showReports,
                showCollaborators = showCollaborators,
                onReportSelected = { selectedReport = it; selectedCollaborator = null },
                onCollaboratorSelected = { selectedCollaborator = it; selectedReport = null },
            )
        }
    }

    // Auto-refresh reports every 60s
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            viewModel.refreshReports()
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

// Use the built-in MAPNIK tile source for a clean light map.
fun lightOsmSource() = TileSourceFactory.MAPNIK

fun MapView.enableMyLocation() {
    val overlay = overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
        ?: MyLocationNewOverlay(GpsMyLocationProvider(context), this).also { overlays.add(it) }
    overlay.enableMyLocation()
    invalidate()
}

private fun MapView.renderReports(
    reports: List<NearbyReport>,
    collaborators: List<com.example.sanosysalvosv2.viewmodel.MapsViewModel.CollaboratorMarker>,
    selectedCategories: Set<MapLegendCategory>,
    showReports: Boolean,
    showCollaborators: Boolean,
    onReportSelected: (NearbyReport?) -> Unit,
    onCollaboratorSelected: (com.example.sanosysalvosv2.viewmodel.MapsViewModel.CollaboratorMarker?) -> Unit,
) {
    // TODO: add per-report radius Polygon once NearbyReportMarker gains a radiusMeters field.
    overlays.removeAll { it is Marker }
    if (showReports) {
        reports.forEach { report ->
            val category = report.toLegendCategory() ?: return@forEach
            if (!selectedCategories.contains(category)) return@forEach
            val isLost = category == MapLegendCategory.Lost

            val markerBitmap = createMarkerBitmap(
                context = context,
                color = android.graphics.Color.parseColor(if (isLost) "#E53935" else "#4A9B8E"),
                label = category.marker,
            )
            val markerDrawable = BitmapDrawable(context.resources, markerBitmap)

            Marker(this).apply {
                position = GeoPoint(report.lat, report.lon)
                icon = markerDrawable
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = report.title
                setInfoWindow(null)
                setOnMarkerClickListener { _, _ ->
                    onReportSelected(report)
                    true
                }
            }.also { overlays.add(it) }
        }
    }

    if (showCollaborators) {
        collaborators.forEach { collab ->
            val typeUpper = collab.type.uppercase()
            val (color, label) = when {
                typeUpper.contains("VETER") || typeUpper.contains("CLINIC") -> Pair("#4A9B8E", "V")
                typeUpper.contains("SHELTER") || typeUpper.contains("REFUGIO") -> Pair("#FF8C00", "R")
                else -> Pair("#1976D2", "M")
            }

            val bmp = createMarkerBitmap(
                context = context,
                color = android.graphics.Color.parseColor(color),
                label = label,
            )
            val drawable = BitmapDrawable(context.resources, bmp)

            Marker(this).apply {
                position = GeoPoint(collab.lat, collab.lng)
                icon = drawable
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = collab.name
                setInfoWindow(null)
                setOnMarkerClickListener { _, _ ->
                    onCollaboratorSelected(collab)
                    true
                }
            }.also { overlays.add(it) }
        }
    }
    invalidate()
}

private fun createMarkerBitmap(
    context: Context,
    color: Int,
    label: String,
): Bitmap {
    val width = 120
    val height = 150
    val circleRadius = 42f
    val centerX = width / 2f
    val centerY = circleRadius + 8f
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, circleRadius, paint)

    val pointerPath = Path().apply {
        moveTo(centerX - 18f, centerY + circleRadius - 4f)
        lineTo(centerX, height - 16f)
        lineTo(centerX + 18f, centerY + circleRadius - 4f)
        close()
    }
    canvas.drawPath(pointerPath, paint)

    paint.apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    canvas.drawPath(pointerPath, paint)

    paint.apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        textSize = 44f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    val textY = centerY - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(label, centerX, textY, paint)

    return bitmap
}

private fun updateMapCenter(mapView: MapView?, lat: Double, lon: Double) {
    mapView?.controller?.setCenter(GeoPoint(lat, lon))
}

private fun NearbyReport.toLegendCategory(): MapLegendCategory? {
    val statusLower = status.lowercase(Locale.getDefault())
    val descriptionLower = description.lowercase(Locale.getDefault())
    return when {
        statusLower.contains("lost") || descriptionLower.contains("lost") -> MapLegendCategory.Lost
        statusLower.contains("found") || descriptionLower.contains("found") -> MapLegendCategory.Found
        else -> MapLegendCategory.Found
    }
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

private fun deriveReportType(selectedCategories: Set<MapLegendCategory>): String? {
    val hasLost = selectedCategories.contains(MapLegendCategory.Lost)
    val hasFound = selectedCategories.contains(MapLegendCategory.Found)

    return when {
        hasLost && !hasFound -> "LOST"
        hasFound && !hasLost -> "FOUND"
        else -> null
    }
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
private fun MapLegend(
    selectedCategories: Set<MapLegendCategory>,
    onCategoryToggle: (MapLegendCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MapLegendCategory.Lost.renderLegendChip(selectedCategories, onCategoryToggle)
            MapLegendCategory.Found.renderLegendChip(selectedCategories, onCategoryToggle)
            MapLegendCategory.Veterinarias.renderLegendChip(selectedCategories, onCategoryToggle)
            MapLegendCategory.Refugios.renderLegendChip(selectedCategories, onCategoryToggle)
            MapLegendCategory.Municipios.renderLegendChip(selectedCategories, onCategoryToggle)
        }
    }
}

@Composable
private fun MapLegendCategory.renderLegendChip(
    selectedCategories: Set<MapLegendCategory>,
    onCategoryToggle: (MapLegendCategory) -> Unit,
) {
    val selected = selectedCategories.contains(this)
    val backgroundColor = if (selected) color.copy(alpha = 0.16f) else Color.Transparent
    val borderColor = if (selected) color else Color(0xFFBDBDBD)
    val contentColor = if (selected) color else Color.Gray

    Row(
        modifier = Modifier
            .clickable { onCategoryToggle(this) }
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(borderColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = marker,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFF4A9B8E)),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}