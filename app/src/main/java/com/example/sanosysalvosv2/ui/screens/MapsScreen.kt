package com.example.sanosysalvosv2.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sanosysalvosv2.R
import com.example.sanosysalvosv2.model.CollaboratorResponse
import com.example.sanosysalvosv2.model.NearbyReport
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
import java.text.Normalizer
import java.util.Locale

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
    Veterinarias("V", "Veterinarias", Color(0xFF1565C0), isCollaborator = true),
    Refugios("R", "Refugios", Color(0xFFE65100), isCollaborator = true),
    Municipios("M", "Municipios", Color(0xFF6A1B9A), isCollaborator = true),
    Voluntarios("U", "Voluntarios", Color(0xFF2E7D32), isCollaborator = true),
    Otros("O", "Otros", Color(0xFF546E7A), isCollaborator = true),
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
    var selectedCollab by remember { mutableStateOf<CollaboratorResponse?>(null) }
    
    var selectedCategories by remember {
        mutableStateOf(
            setOf(
                MapLegendCategory.Lost,
                MapLegendCategory.Found,
                MapLegendCategory.Veterinarias,
                MapLegendCategory.Refugios,
                MapLegendCategory.Municipios,
                MapLegendCategory.Voluntarios,
                MapLegendCategory.Otros,
            ),
        )
    }

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
                update = { map -> map.invalidate() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            )

            if (uiState is MapsUiState.Loading || uiState is MapsUiState.AwaitingLocation) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Info banners
            InfoBanners(uiState, Modifier.align(Alignment.TopCenter))

            // Map Legend con Contadores
            if (selectedReport == null && selectedCollab == null) {
                val lostCount = (uiState as? MapsUiState.Success)?.lostCount ?: 0
                val foundCount = (uiState as? MapsUiState.Success)?.foundCount ?: 0
                
                MapLegend(
                    selectedCategories = selectedCategories,
                    lostCount = lostCount,
                    foundCount = foundCount,
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

            // FAB Mi Ubicación
            FloatingActionButton(
                onClick = { resolveLocationAndLoad(context, mapView, viewModel) },
                containerColor = Color(0xFF4A9B8E),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
            ) {
                Icon(Icons.Default.LocationOn, "Mi ubicación")
            }

            // Report info card
            selectedReport?.let { report ->
                ReportInfoCard(
                    report = report,
                    onClose = { selectedReport = null },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Collaborator info card
            selectedCollab?.let { collab ->
                CollaboratorInfoCard(
                    collab = collab,
                    onClose = { selectedCollab = null },
                    onCall = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Marker Rendering Effect
    LaunchedEffect(uiState, collaborators, selectedCategories, mapView) {
        val currentMapView = mapView ?: return@LaunchedEffect
        currentMapView.overlays.removeAll { it is Marker }
        
        // Render Reports
        if (uiState is MapsUiState.Success) {
            val reports = (uiState as MapsUiState.Success).reports
            reports.forEach { report ->
                val category = report.toLegendCategory() ?: return@forEach
                if (!selectedCategories.contains(category)) return@forEach
                
                val marker = Marker(currentMapView)
                marker.position = GeoPoint(report.lat, report.lon)
                marker.icon = BitmapDrawable(
                    context.resources,
                    createMarkerBitmap(
                        context,
                        android.graphics.Color.parseColor(if (category == MapLegendCategory.Lost) "#E53935" else "#4A9B8E"),
                        category.marker
                    )
                )
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = report.title
                marker.setOnMarkerClickListener { _, _ ->
                    selectedReport = report
                    selectedCollab = null
                    true
                }
                currentMapView.overlays.add(marker)
            }
        }

        // Render Collaborators
        collaborators.forEach { collab ->
            val category = resolveCollaboratorCategory(collab.type)
            if (category == null || !selectedCategories.contains(category)) return@forEach

            val coordinates = resolveCollaboratorCoordinates(collab, viewModel.comunaCoordinates)
            if (coordinates == null) return@forEach

            val (lat, lon) = coordinates
            val marker = Marker(currentMapView)
            marker.position = GeoPoint(lat, lon)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            val (color, label) = resolveCollaboratorMarkerAppearance(collab.type)
            marker.icon = BitmapDrawable(context.resources, createCollabMarkerBitmap(color, label))
            marker.title = collab.name
            marker.setOnMarkerClickListener { _, _ ->
                selectedCollab = collab
                selectedReport = null
                true
            }
            currentMapView.overlays.add(marker)
        }
        currentMapView.invalidate()
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

@Composable
private fun InfoBanners(uiState: MapsUiState, modifier: Modifier) {
    val bannerText = when (uiState) {
        is MapsUiState.AwaitingLocation -> "Esperando ubicación para consultar reportes cercanos..."
        is MapsUiState.Loading -> "Cargando reportes cercanos..."
        is MapsUiState.Success -> if (uiState.reports.isEmpty()) "No hay reportes cercanos en tu zona por ahora." else null
        is MapsUiState.Error -> uiState.message
    }
    
    bannerText?.let {
        InfoBanner(
            text = it,
            modifier = modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun ReportInfoCard(
    report: NearbyReport,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLost = report.status.uppercase().contains("LOST") || report.description.lowercase().contains("lost")
    val statusLabel = if (isLost) "PERDIDO" else "ENCONTRADO"
    val statusColor = if (isLost) Color(0xFFC62828) else Color(0xFF4A9B8E)

    Surface(
        modifier = modifier
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

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Color.Gray)
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

            // Lógica de Foto Restaurada
            val photoBitmap = remember(report.photoBase64) {
                report.photoBase64?.takeIf { it.isNotBlank() }?.let { base64Data ->
                    try {
                        val base64 = base64Data.replaceFirst(Regex("^data:image/[^;]+;base64,"), "")
                        val bytes = Base64.decode(base64, Base64.NO_WRAP)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            if (photoBitmap != null) {
                Image(
                    bitmap = photoBitmap.asImageBitmap(),
                    contentDescription = "Foto del reporte",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            } else if (!report.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = report.photoUrl,
                    contentDescription = "Foto del reporte",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFB2DFDB)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Pets, null, tint = Color.White, modifier = Modifier.size(34.dp))
                }
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

@Composable
private fun CollaboratorInfoCard(
    collab: CollaboratorResponse,
    onClose: () -> Unit,
    onCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (badgeColor, badgeText) = when(collab.type.uppercase()) {
                    "VETERINARY_CLINIC" -> Pair(Color(0xFF1565C0), "Clínica Vet.")
                    "SHELTER" -> Pair(Color(0xFFE65100), "Refugio")
                    "MUNICIPALITY" -> Pair(Color(0xFF6A1B9A), "Municipalidad")
                    "VOLUNTEER" -> Pair(Color(0xFF2E7D32), "Voluntariado")
                    else -> Pair(Color.Gray, "Entidad")
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        badgeText,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Text(collab.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2D2D))
            Spacer(Modifier.height(4.dp))
            
            if (collab.comuna.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4A9B8E), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(collab.comuna, fontSize = 13.sp, color = Color.Gray)
                }
            }
            
            if (collab.address.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, null, tint = Color(0xFF4A9B8E), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(collab.address, fontSize = 13.sp, color = Color.Gray)
                }
            }
            
            if (collab.phone.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onCall(collab.phone) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF4A9B8E)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A9B8E))
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar: ${collab.phone}")
                }
            }
        }
    }
}

fun MapView.enableMyLocation() {
    val overlay = overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
        ?: MyLocationNewOverlay(GpsMyLocationProvider(context), this).also { overlays.add(it) }
    overlay.enableMyLocation()
    invalidate()
}

private fun createMarkerBitmap(context: Context, color: Int, label: String): Bitmap {
    val width = 120
    val height = 150
    val circleRadius = 42f
    val centerX = width / 2f
    val centerY = circleRadius + 8f
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    val pointerPath = Path().apply {
        moveTo(centerX - 18f, centerY + circleRadius - 4f)
        lineTo(centerX, height - 16f)
        lineTo(centerX + 18f, centerY + circleRadius - 4f)
        close()
    }
    canvas.drawPath(pointerPath, paint)
    paint.apply { this.color = android.graphics.Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 6f }
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    canvas.drawPath(pointerPath, paint)
    paint.apply { this.color = android.graphics.Color.WHITE; style = Paint.Style.FILL; textSize = 44f; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
    val textY = centerY - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(label, centerX, textY, paint)
    return bitmap
}

fun createCollabMarkerBitmap(color: Int, label: String): Bitmap {
    val size = 100
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color
    paint.style = Paint.Style.FILL
    val rect = RectF(4f, 4f, size - 4f, size - 4f)
    canvas.drawRoundRect(rect, 16f, 16f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 5f
    canvas.drawRoundRect(rect, 16f, 16f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.FILL
    paint.textSize = 38f
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.DEFAULT_BOLD
    canvas.drawText(label, size / 2f, size / 2f + 13f, paint)
    return bitmap
}

fun translateCollabType(type: String): String = when(type.uppercase()) {
    "VETERINARY_CLINIC" -> "Clínica Veterinaria"
    "SHELTER" -> "Refugio"
    "MUNICIPALITY" -> "Municipalidad"
    "VOLUNTEER" -> "Voluntariado"
    else -> "Entidad"
}

private fun resolveCollaboratorCategory(type: String): MapLegendCategory? {
    val normalized = type.trim().uppercase(Locale.getDefault())
    return when {
        normalized.contains("VETER") || normalized.contains("CLIN") || normalized.contains("CLÍN") -> MapLegendCategory.Veterinarias
        normalized.contains("SHELTER") || normalized.contains("REFUGIO") -> MapLegendCategory.Refugios
        normalized.contains("MUNIC") -> MapLegendCategory.Municipios
        normalized.contains("VOLUN") -> MapLegendCategory.Voluntarios
        normalized.isBlank() -> null
        else -> MapLegendCategory.Otros
    }
}

private fun resolveCollaboratorMarkerAppearance(type: String): Pair<Int, String> {
    val normalized = type.trim().uppercase(Locale.getDefault())
    return when {
        normalized.contains("VETER") || normalized.contains("CLIN") || normalized.contains("CLÍN") -> Pair(android.graphics.Color.parseColor("#1565C0"), "V")
        normalized.contains("SHELTER") || normalized.contains("REFUGIO") -> Pair(android.graphics.Color.parseColor("#E65100"), "R")
        normalized.contains("MUNIC") -> Pair(android.graphics.Color.parseColor("#6A1B9A"), "M")
        normalized.contains("VOLUN") -> Pair(android.graphics.Color.parseColor("#2E7D32"), "U")
        else -> Pair(android.graphics.Color.parseColor("#546E7A"), "O")
    }
}

private fun resolveCollaboratorCoordinates(
    collab: CollaboratorResponse,
    comunaCoordinates: Map<String, Pair<Double, Double>>,
): Pair<Double, Double>? {
    val latitude = collab.latitude
    val longitude = collab.longitude
    if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0 && !latitude.isNaN() && !longitude.isNaN()) {
        return Pair(latitude, longitude)
    }
    return resolveComunaCoordinates(collab.comuna, comunaCoordinates)
        ?: resolveComunaCoordinates(collab.address, comunaCoordinates)
}

private fun resolveComunaCoordinates(
    comuna: String,
    comunaCoordinates: Map<String, Pair<Double, Double>>,
): Pair<Double, Double>? {
    if (comuna.isBlank()) return null
    val normalized = normalizeComuna(comuna)

    for ((key, coords) in comunaCoordinates) {
        if (normalizeComuna(key) == normalized) return coords
    }

    // Allow broader matches
    for ((key, coords) in comunaCoordinates) {
        val normalizedKey = normalizeComuna(key)
        if (normalized.contains(normalizedKey) || normalizedKey.contains(normalized)) return coords
    }

    return null
}

private fun normalizeComuna(value: String): String {
    val base = Normalizer.normalize(value.trim().lowercase(Locale.getDefault()), Normalizer.Form.NFD)
    return base.replace("""\p{InCombiningDiacriticalMarks}+""".toRegex(), "").replace("""[\s]+""".toRegex(), " ")
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
    lostCount: Int,
    foundCount: Int,
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
            MapLegendCategory.Lost.renderLegendChip(selectedCategories, lostCount, onCategoryToggle)
            MapLegendCategory.Found.renderLegendChip(selectedCategories, foundCount, onCategoryToggle)
            MapLegendCategory.Veterinarias.renderLegendChip(selectedCategories, null, onCategoryToggle)
            MapLegendCategory.Refugios.renderLegendChip(selectedCategories, null, onCategoryToggle)
            MapLegendCategory.Municipios.renderLegendChip(selectedCategories, null, onCategoryToggle)
            MapLegendCategory.Voluntarios.renderLegendChip(selectedCategories, null, onCategoryToggle)
            MapLegendCategory.Otros.renderLegendChip(selectedCategories, null, onCategoryToggle)
        }
    }
}

@Composable
private fun MapLegendCategory.renderLegendChip(
    selectedCategories: Set<MapLegendCategory>,
    count: Int?,
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
            text = if (count != null) "$label ($count)" else label,
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
