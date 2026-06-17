package com.example.sanosysalvosv2.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.util.TranslationUtils
import com.example.sanosysalvosv2.viewmodel.UserReportsUiState
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel
import com.google.android.gms.location.LocationServices
import java.io.ByteArrayOutputStream
import java.util.Calendar

@Composable
fun EditReportScreen(
    reportViewModel: UserReportsViewModel,
    reportId: String,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val selectedReport by reportViewModel.selectedReport.collectAsState()
    val uiState by reportViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var type by remember { mutableStateOf(selectedReport?.type ?: "LOST") }
    var eventDate by remember { mutableStateOf(selectedReport?.eventDate ?: "") }
    var locationName by remember { mutableStateOf(selectedReport?.locationName ?: "") }
    var description by remember { mutableStateOf(selectedReport?.description ?: "") }
    
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                eventDate = String.format(
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).also { dialog ->
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }
    }

    var latitude by remember { mutableStateOf(selectedReport?.latitude ?: 0.0) }
    var longitude by remember { mutableStateOf(selectedReport?.longitude ?: 0.0) }
    var locationObtained by remember { 
      mutableStateOf(selectedReport?.latitude != null && selectedReport?.latitude != 0.0) 
    }

    var photoBase64 by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var initialized by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
      LocationServices.getFusedLocationProviderClient(context)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
    ) { uri ->
      uri?.let { selectedUri ->
        try {
          val inputStream = context.contentResolver.openInputStream(selectedUri)
          val bytes = inputStream?.readBytes()
          inputStream?.close()
          if (bytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val maxDim = 800
            val scale = minOf(
              maxDim.toFloat() / bitmap.width,
              maxDim.toFloat() / bitmap.height,
              1.0f
            )
            val resized = if (scale < 1f) {
              Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
              )
            } else bitmap
            val out = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 70, out)
            photoBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            photoUri = selectedUri
            if (resized != bitmap) resized.recycle()
            bitmap.recycle()
          }
        } catch (e: Exception) {
          Log.e("ReportPhoto", "Error: ${e.message}")
        }
      }
    }

    LaunchedEffect(reportId) {
        reportViewModel.loadReportDetails(reportId)
    }

    LaunchedEffect(selectedReport) {
        if (selectedReport != null && !initialized) {
            type = selectedReport?.type ?: "LOST"
            eventDate = selectedReport?.eventDate ?: ""
            locationName = selectedReport?.locationName ?: ""
            description = selectedReport?.description ?: ""
            latitude = selectedReport?.latitude ?: 0.0
            longitude = selectedReport?.longitude ?: 0.0
            locationObtained = selectedReport?.latitude != null && selectedReport?.latitude != 0.0
            initialized = true
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UserReportsUiState.Updated) {
            onSaved()
        }
    }

    LaunchedEffect(Unit) {
      try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
          location?.let {
            latitude = it.latitude
            longitude = it.longitude
            locationObtained = true
          }
        }
      } catch (e: SecurityException) {
        // permission not granted
      }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateBack() },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Editar reporte",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState is UserReportsUiState.Loading && selectedReport == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // 1. Type chip (read-only)
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Tipo:", fontWeight = FontWeight.Medium, color = Color.Gray)
              Spacer(Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (type == "LOST") 
                  Color(0xFFE53935).copy(alpha = 0.12f)
                else 
                  Color(0xFF4A9B8E).copy(alpha = 0.12f)
              ) {
                Text(
                  text = if (type == "LOST") "Mascota perdida" else "Mascota encontrada",
                  color = if (type == "LOST") Color(0xFFE53935) else Color(0xFF4A9B8E),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  fontWeight = FontWeight.Medium,
                  fontSize = 13.sp
                )
              }
            }

            // 2. Descripción (multiline TextField)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
            )

            // 3. Ubicación / locationName (TextField)
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // 4. GPS location status row (auto)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (locationObtained) Color(0xFF4A9B8E).copy(alpha = 0.1f)
                  else Color(0xFFFFF3E0)
                )
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                if (locationObtained) Icons.Default.LocationOn 
                else Icons.Default.LocationOff,
                contentDescription = null,
                tint = if (locationObtained) Color(0xFF4A9B8E) else Color(0xFFFF8C00),
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.width(8.dp))
              Text(
                if (locationObtained) 
                  "Ubicación GPS obtenida ✓"
                else 
                  "No se pudo obtener ubicación GPS",
                fontSize = 13.sp,
                color = if (locationObtained) Color(0xFF4A9B8E) else Color(0xFFFF8C00)
              )
              Spacer(Modifier.weight(1f))
              // Refresh location button
              IconButton(
                onClick = {
                  try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                      loc?.let {
                        latitude = it.latitude
                        longitude = it.longitude
                        locationObtained = true
                      }
                    }
                  } catch (e: SecurityException) {}
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  Icons.Default.Refresh,
                  contentDescription = "Actualizar ubicación",
                  tint = Color(0xFF4A9B8E),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            // 5. Fecha del evento
            Column {
                Text(
                    "Fecha del evento",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, 
                        if (eventDate.isNotEmpty()) Color(0xFF4A9B8E) 
                        else Color.LightGray
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (eventDate.isNotEmpty()) 
                            Color(0xFF2D6A5F) 
                        else Color.Gray
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (eventDate.isNotEmpty()) eventDate 
                        else "Seleccionar fecha",
                        fontSize = 15.sp
                    )
                }
            }

            // 6. Photo section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Foto", fontWeight = FontWeight.Medium, color = Color.Gray, fontSize = 12.sp)
              
              if (photoUri != null) {
                AsyncImage(
                  model = photoUri,
                  contentDescription = null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                  contentScale = ContentScale.Crop
                )
              } else if (!selectedReport?.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = selectedReport?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
              }
              
              OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFF4A9B8E)),
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = Color(0xFF4A9B8E)
                )
              ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text(if (photoUri != null || !selectedReport?.photoUrl.isNullOrBlank()) "Cambiar foto" else "Agregar foto")
              }
            }

            if (uiState is UserReportsUiState.Error) {
                Text(
                    text = (uiState as UserReportsUiState.Error).message,
                    color = Color(0xFFD32F2F),
                )
            }

            if (uiState is UserReportsUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // 7. Guardar button
                PrimaryButton(
                    text = "Guardar cambios",
                    onClick = {
                        val updateBody = mutableMapOf<String, Any?>()
                        updateBody["description"] = description.trim()
                        updateBody["locationName"] = locationName.trim().takeIf { it.isNotBlank() }
                        updateBody["eventDate"] = eventDate.trim()
                        updateBody["latitude"] = latitude
                        updateBody["longitude"] = longitude
                        if (photoBase64.isNotEmpty()) {
                            updateBody["photoBase64"] = photoBase64
                        }
                        reportViewModel.updateReportFields(reportId, updateBody)
                    },
                )
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun uriToBase64(context: Context, uri: Uri): String = try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
    } ?: ""
} catch (_: Exception) {
    ""
}
