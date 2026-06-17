package com.example.sanosysalvosv2.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.model.PetRequest
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.PetsUiState
import com.example.sanosysalvosv2.viewmodel.PetsViewModel

@Composable
fun AddEditPetScreen(
    petViewModel: PetsViewModel,
    petId: String?,
    initialPet: PetResponse?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var name by remember { mutableStateOf(initialPet?.name.orEmpty()) }

    val speciesOptions = listOf(
        "Perro" to "DOG",
        "Gato" to "CAT",
        "Otro" to "OTHER",
    )
    val defaultSpecies = speciesOptions.firstOrNull { option ->
        option.first.equals(initialPet?.species, true) || option.second.equals(initialPet?.species, true)
    } ?: speciesOptions.first()
    var selectedSpeciesLabel by remember { mutableStateOf(defaultSpecies.first) }
    var selectedSpeciesValue by remember { mutableStateOf(defaultSpecies.second) }

    var breed by remember { mutableStateOf(initialPet?.breed.orEmpty()) }

    val genderOptions = listOf(
        "Macho" to "MALE",
        "Hembra" to "FEMALE",
    )
    val defaultGender = genderOptions.firstOrNull { option ->
        option.first.equals(initialPet?.sex, true) || option.second.equals(initialPet?.sex, true)
    } ?: genderOptions.first()
    var selectedSexLabel by remember { mutableStateOf(defaultGender.first) }
    var selectedSexValue by remember { mutableStateOf(defaultGender.second) }

    var age by remember { mutableStateOf(initialPet?.age?.toString().orEmpty()) }
    var color by remember { mutableStateOf(initialPet?.color.orEmpty()) }

    val sizeOptions = listOf(
        "Pequeño" to "SMALL",
        "Mediano" to "MEDIUM",
        "Grande" to "LARGE",
    )
    val defaultSize = sizeOptions.firstOrNull { option ->
        option.first.equals(initialPet?.size, true) || option.second.equals(initialPet?.size, true)
    } ?: sizeOptions.first()
    var selectedSizeLabel by remember { mutableStateOf(defaultSize.first) }
    var selectedSizeValue by remember { mutableStateOf(defaultSize.second) }

    var hasMicrochip by remember { mutableStateOf(initialPet?.hasMicrochip ?: false) }
    var isNeutered by remember { mutableStateOf(initialPet?.isNeutered ?: false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBase64 by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val uiState by petViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val originalBytes = inputStream?.readBytes()
                inputStream?.close()

                if (originalBytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                    if (bitmap != null) {
                        val maxDim = 800
                        val scale = minOf(
                            maxDim.toFloat() / bitmap.width,
                            maxDim.toFloat() / bitmap.height,
                            1.0f,
                        )
                        val resized = if (scale < 1.0f) {
                            Bitmap.createScaledBitmap(
                                bitmap,
                                (bitmap.width * scale).toInt(),
                                (bitmap.height * scale).toInt(),
                                true,
                            )
                        } else {
                            bitmap
                        }

                        val outputStream = java.io.ByteArrayOutputStream()
                        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                        photoBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                        photoUri = selectedUri

                        if (resized != bitmap) resized.recycle()
                        bitmap.recycle()
                    }
                }
            } catch (e: Exception) {
                Log.e("PetPhoto", "Error converting image: ${e.message}")
            }
        }
    }

    LaunchedEffect(uiState) {
        message = when (uiState) {
            is PetsUiState.Saved -> {
                onSaved()
                ""
            }
            is PetsUiState.Error -> (uiState as PetsUiState.Error).message
            else -> ""
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    text = if (petId == null) "Agregar mascota" else "Editar mascota",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            DropdownField(
                label = "Especie",
                selected = selectedSpeciesLabel,
                options = speciesOptions.map { it.first },
                onSelected = { selectedLabel ->
                    selectedSpeciesLabel = selectedLabel
                    selectedSpeciesValue = speciesOptions.first { option -> option.first == selectedLabel }.second
                },
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            DropdownField(
                label = "Sexo",
                selected = selectedSexLabel,
                options = genderOptions.map { it.first },
                onSelected = { selectedLabel ->
                    selectedSexLabel = selectedLabel
                    selectedSexValue = genderOptions.first { option -> option.first == selectedLabel }.second
                },
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { char -> char.isDigit() } },
                label = { Text("Edad en años") },
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

            DropdownField(
                label = "Tamaño",
                selected = selectedSizeLabel,
                options = sizeOptions.map { it.first },
                onSelected = { selectedLabel ->
                    selectedSizeLabel = selectedLabel
                    selectedSizeValue = sizeOptions.first { option -> option.first == selectedLabel }.second
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Microchip",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = hasMicrochip,
                    onCheckedChange = { hasMicrochip = it },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Esterilizado",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = isNeutered,
                    onCheckedChange = { isNeutered = it },
                )
            }

            PrimaryButton(
                text = "Seleccionar foto",
                onClick = { photoPicker.launch("image/*") },
            )

            photoUri?.let { uri ->
                val bitmap = remember(uri) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Foto mascota",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    color = if (uiState is PetsUiState.Error) Color(0xFFD32F2F) else TextAccent,
                )
            }

            when (uiState) {
                is PetsUiState.Loading -> CircularProgressIndicator()
                else -> PrimaryButton(
                    text = if (petId == null) "Guardar" else "Actualizar",
                    onClick = {
                        val request = PetRequest(
                            name = name.trim(),
                            species = selectedSpeciesValue,
                            breed = breed.trim(),
                            sex = selectedSexValue,
                            age = age.toIntOrNull() ?: 0,
                            color = color.trim(),
                            size = selectedSizeValue,
                            isNeutered = isNeutered,
                            photoBase64 = photoBase64,
                        )
                        if (petId == null) {
                            petViewModel.createPet(request)
                        } else {
                            petViewModel.updatePet(petId, request)
                        }
                    },
                )
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

private fun uriToBase64(context: Context, uri: Uri): String = try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
    } ?: ""
} catch (_: Exception) {
    ""
}
