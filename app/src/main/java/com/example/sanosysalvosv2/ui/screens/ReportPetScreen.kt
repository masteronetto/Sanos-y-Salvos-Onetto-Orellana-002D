package com.example.sanosysalvosv2.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.model.PetReportRequest
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.viewmodel.PetReportUiState
import com.example.sanosysalvosv2.viewmodel.PetViewModel

@Composable
fun ReportPetScreen(
    petViewModel: PetViewModel,
    onNavigateBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("LOST") }
    var species by remember { mutableStateOf("dog") }
    var breed by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("MEDIUM") }
    var description by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBase64 by remember { mutableStateOf("") }

    val uiState by petViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            photoUri = it
            photoBase64 = uriToBase64(context, it)
        }
    }

    // Auto-fill location from last known position and reset ViewModel state on entry
    LaunchedEffect(Unit) {
        petViewModel.resetState()
        val location = resolveLastLocation(context)
        if (location != null) {
            lat = location.latitude
            lng = location.longitude
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Reportar mascota", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre de la mascota") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        PetDropdownField(
            label = "Estado",
            selected = status,
            options = listOf("LOST", "FOUND"),
            onSelected = { status = it },
        )

        PetDropdownField(
            label = "Especie",
            selected = species,
            options = listOf("dog", "cat", "other"),
            onSelected = { species = it },
        )

        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("Raza") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        PetDropdownField(
            label = "Tamaño",
            selected = size,
            options = listOf("SMALL", "MEDIUM", "LARGE"),
            onSelected = { size = it },
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        Text(
            text = if (lat != 0.0 && lng != 0.0)
                "Ubicación: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
            else
                "Ubicación no disponible",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
        )

        PrimaryButton(
            text = "Seleccionar foto",
            onClick = { photoPicker.launch("image/*") },
        )

        // Thumbnail preview of selected photo
        photoUri?.let { uri ->
            val bitmap = remember(uri) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    null
                }
            }
            bitmap?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    bitmap = it,
                    contentDescription = "Foto seleccionada",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        // State feedback
        when (val state = uiState) {
            is PetReportUiState.Error -> Text(
                text = state.message,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
            )
            is PetReportUiState.Success -> Text(
                text = "Reporte creado exitosamente. ID: ${state.id}",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodySmall,
            )
            else -> {}
        }

        if (uiState is PetReportUiState.Loading) {
            CircularProgressIndicator()
        } else {
            PrimaryButton(
                text = "Enviar reporte",
                onClick = {
                    petViewModel.submitReport(
                        PetReportRequest(
                            name = name.trim(),
                            status = status,
                            species = species,
                            breed = breed.trim(),
                            color = color.trim(),
                            size = size,
                            lat = lat,
                            lng = lng,
                            description = description.trim(),
                            photoBase64 = photoBase64,
                        )
                    )
                },
            )
        }

        PrimaryButton(text = "Volver", onClick = onNavigateBack)
    }
}

@Composable
private fun PetDropdownField(
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
        // Invisible overlay captures the tap and opens the menu
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

@SuppressLint("MissingPermission")
private fun resolveLastLocation(context: Context): android.location.Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { lm.isProviderEnabled(it) }
        .mapNotNull { lm.getLastKnownLocation(it) }
        .maxByOrNull { it.time }
}

private fun uriToBase64(context: Context, uri: Uri): String = try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
    } ?: ""
} catch (e: Exception) {
    ""
}
