package com.example.sanosysalvosv2.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.example.sanosysalvosv2.model.ReportTypes

private val speciesOptions = listOf(
    "Perro" to "DOG",
    "Gato" to "CAT",
    "Otro" to "OTHER",
)

private val sizeOptions = listOf(
    "Pequeño" to "SMALL",
    "Mediano" to "MEDIUM",
    "Grande" to "LARGE",
)

private val reportTypeOptions = listOf(
    ReportTypes.LOST to "Perdida",
    ReportTypes.FOUND to "Encontrada",
)

data class ReportFormState(
    val type: String = ReportTypes.LOST,
    val petName: String = "",
    val petId: String? = null,
    val description: String = "",
    val locationName: String = "",
    val eventDate: String = "",
    val selectedSpeciesLabel: String = "Seleccionar especie",
    val selectedSpeciesValue: String = "",
    val breed: String = "",
    val color: String = "",
    val selectedSizeLabel: String = "Seleccionar tamaño",
    val selectedSizeValue: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoBase64: String = "",
    val existingPhotoUrl: String? = null,
)

@Composable
fun ReportFormContent(
    title: String,
    buttonText: String,
    state: ReportFormState,
    onStateChange: (ReportFormState) -> Unit,
    contactName: String? = null,
    contactPhone: String? = null,
    photoUri: Uri?,
    onPickPhoto: () -> Unit,
    onUpdateLocation: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    validationError: String?,
    errorMessage: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val formattedLatitude = "%.4f".format(state.latitude)
    val formattedLongitude = "%.4f".format(state.longitude)

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format(
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )
                onStateChange(state.copy(eventDate = selectedDate))
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

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }

            Text(text = "Tipo de reporte", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                reportTypeOptions.forEach { (value, label) ->
                    Button(
                        onClick = { onStateChange(state.copy(type = value)) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (state.type == value) Color(0xFF4A9B8E) else Color(0xFFE0E0E0),
                            contentColor = if (state.type == value) Color.White else Color.Black,
                        ),
                    ) {
                        Text(label)
                    }
                }
            }

            OutlinedTextField(
                value = state.petName,
                onValueChange = { onStateChange(state.copy(petName = it)) },
                label = { Text("Nombre de la mascota") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF4A9B8E))
                },
                singleLine = true,
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { onStateChange(state.copy(description = it)) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
            )

            DropdownField(
                label = "Especie",
                selected = state.selectedSpeciesLabel,
                options = speciesOptions.map { it.first },
                onSelected = { selectedLabel ->
                    val selectedValue = speciesOptions.firstOrNull { it.first == selectedLabel }?.second.orEmpty()
                    onStateChange(state.copy(
                        selectedSpeciesLabel = selectedLabel,
                        selectedSpeciesValue = selectedValue,
                    ))
                },
            )

            OutlinedTextField(
                value = state.breed,
                onValueChange = { onStateChange(state.copy(breed = it)) },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.color,
                onValueChange = { onStateChange(state.copy(color = it)) },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            DropdownField(
                label = "Tamaño",
                selected = state.selectedSizeLabel,
                options = sizeOptions.map { it.first },
                onSelected = { selectedLabel ->
                    val selectedValue = sizeOptions.firstOrNull { it.first == selectedLabel }?.second.orEmpty()
                    onStateChange(state.copy(
                        selectedSizeLabel = selectedLabel,
                        selectedSizeValue = selectedValue,
                    ))
                },
            )

            OutlinedTextField(
                value = state.locationName,
                onValueChange = { onStateChange(state.copy(locationName = it)) },
                label = { Text("Nombre del lugar") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
            )

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
                        if (state.eventDate.isNotEmpty()) Color(0xFF4A9B8E) 
                        else Color.LightGray
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.eventDate.isNotEmpty()) 
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
                        if (state.eventDate.isNotEmpty()) state.eventDate 
                        else "Seleccionar fecha",
                        fontSize = 15.sp
                    )
                }
            }

            if (!contactName.isNullOrBlank() || !contactPhone.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Contacto", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = contactName.orEmpty())
                        Text(text = contactPhone.orEmpty())
                    }
                }
            }

            Text(
                text = if (state.latitude != 0.0 && state.longitude != 0.0)
                    "Ubicación actual: $formattedLatitude, $formattedLongitude"
                else
                    "Ubicación no disponible",
                color = Color.Gray,
            )

            Button(onClick = onUpdateLocation) {
                Text("Actualizar ubicación")
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (photoUri == null && state.existingPhotoUrl.isNullOrBlank()) {
                    TextButton(onClick = onPickPhoto) {
                        Text(text = "Seleccionar foto", color = Color(0xFF4A9B8E))
                    }
                } else {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Foto seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else if (!state.existingPhotoUrl.isNullOrBlank()) {
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
                                color = Color(0xFF757575),
                            )
                        }
                    }
                    TextButton(onClick = onPickPhoto) {
                        Text(text = "Cambiar foto", color = Color(0xFF4A9B8E))
                    }
                }
            }

            validationError?.let {
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                )
            }

            if (isSubmitting) {
                CircularProgressIndicator()
            }

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
            ) {
                Text(buttonText)
            }
        }
    }
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
