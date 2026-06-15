package com.example.sanosysalvosv2.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.UserReportsUiState
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel

@Composable
fun EditReportScreen(
    reportViewModel: UserReportsViewModel,
    reportId: String,
    onNavigateBack: () -> Unit,
) {
    val selectedReport by reportViewModel.selectedReport.collectAsState()
    val uiState by reportViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var petId by remember { mutableStateOf<String?>(selectedReport?.species ?: selectedReport?.breed ?: selectedReport?.locationName) }
    var type by remember { mutableStateOf(selectedReport?.type ?: "LOST") }
    var date by remember { mutableStateOf(selectedReport?.eventDate ?: selectedReport?.createdAt ?: "") }
    var location by remember { mutableStateOf(selectedReport?.locationName ?: "") }
    var description by remember { mutableStateOf(selectedReport?.description ?: "") }
    var species by remember { mutableStateOf(selectedReport?.species ?: "") }
    var breed by remember { mutableStateOf(selectedReport?.breed ?: "") }
    var color by remember { mutableStateOf("") }
    var animalCondition by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBase64 by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            photoUri = it
            photoBase64 = uriToBase64(context, it)
        }
    }

    LaunchedEffect(reportId) {
        reportViewModel.loadReportDetails(reportId)
    }

    LaunchedEffect(selectedReport) {
        if (selectedReport != null && !initialized) {
            petId = selectedReport?.species ?: selectedReport?.breed ?: selectedReport?.locationName
            type = selectedReport?.type ?: "LOST"
            date = selectedReport?.eventDate ?: selectedReport?.createdAt ?: ""
            location = selectedReport?.locationName ?: ""
            description = selectedReport?.description ?: ""
            breed = selectedReport?.breed ?: ""
            initialized = true
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                )
            }

            if (uiState is UserReportsUiState.Loading && selectedReport == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            DropdownField(
                label = "Tipo de reporte",
                selected = type,
                options = listOf("LOST", "FOUND"),
                onSelected = { type = it },
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (ISO)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lng") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
            )

            if (type == "LOST") {
                OutlinedTextField(
                    value = petId.orEmpty(),
                    onValueChange = { petId = it },
                    label = { Text("petId") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            } else {
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Especie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

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

            OutlinedTextField(
                value = animalCondition,
                onValueChange = { animalCondition = it },
                label = { Text("Condición animal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            SectionTitle("Foto del reporte")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (photoUri == null && selectedReport?.photoUrl.isNullOrBlank()) {
                    // No photo selected and no existing photo
                    TextButton(onClick = { photoPicker.launch("image/*") }) {
                        Text(text = "Seleccionar foto", color = TextAccent)
                    }
                } else {
                    // Show existing photo or selected photo with change option
                    if (photoUri != null) {
                        val bitmap = remember(photoUri) {
                            try {
                                context.contentResolver.openInputStream(photoUri!!)?.use { stream ->
                                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                }
                            } catch (_: Exception) {
                                null
                            }
                        }
                        bitmap?.let {
                            Image(
                                bitmap = it,
                                contentDescription = "Foto seleccionada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    } else if (!selectedReport?.photoUrl.isNullOrBlank()) {
                        // Show placeholder for existing photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEDEDED)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Foto existente",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    TextButton(onClick = { photoPicker.launch("image/*") }) {
                        Text(text = "Cambiar foto", color = TextAccent)
                    }
                }
            }

            if (uiState is UserReportsUiState.Error) {
                Text(
                    text = (uiState as UserReportsUiState.Error).message,
                    color = Color(0xFFD32F2F),
                )
            }

            if (uiState is UserReportsUiState.Loading) {
                CircularProgressIndicator()
            } else {
                PrimaryButton(
                    text = "Guardar cambios",
                    onClick = {
                        // Build update body with only modified fields
                        val updateBody = mutableMapOf<String, String>()
                        updateBody["description"] = description
                        updateBody["locationName"] = location
                        updateBody["eventDate"] = date
                        if (photoBase64.isNotEmpty()) {
                            updateBody["photoBase64"] = photoBase64
                        }
                        reportViewModel.updateReportFields(reportId, updateBody)
                    },
                )
            }

            PrimaryButton(text = "Volver", onClick = onNavigateBack)
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
