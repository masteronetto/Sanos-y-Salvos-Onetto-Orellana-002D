package com.example.sanosysalvosv2.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
// Removed KeyboardOptions/KeyboardType imports (not used)
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.model.PetRequest
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.AdminMascotasUiState
import com.example.sanosysalvosv2.viewmodel.AdminMascotasViewModel

@Composable
fun AdminEditPetScreen(
    petId: String?,
    onNavigateBack: () -> Unit,
) {
    val viewModel: AdminMascotasViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllPets()
        petId?.let { viewModel.loadPetDetails(it) }
    }

    var name by remember { mutableStateOf(selectedPet?.name.orEmpty()) }
    var species by remember { mutableStateOf(selectedPet?.species ?: "Perro") }
    var breed by remember { mutableStateOf(selectedPet?.breed.orEmpty()) }
    var sex by remember { mutableStateOf(selectedPet?.sex ?: "Macho") }
    var age by remember { mutableStateOf(selectedPet?.age?.toString().orEmpty()) }
    var color by remember { mutableStateOf(selectedPet?.color.orEmpty()) }
    var size by remember { mutableStateOf(selectedPet?.size ?: "Mediano") }
    var hasMicrochip by remember { mutableStateOf(selectedPet?.hasMicrochip ?: false) }
    var isNeutered by remember { mutableStateOf(selectedPet?.isNeutered ?: false) }
    var ownerId by remember { mutableStateOf(selectedPet?.ownerId.orEmpty()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBase64 by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

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

    LaunchedEffect(selectedPet) {
        if (selectedPet != null) {
            name = selectedPet?.name.orEmpty()
            species = selectedPet?.species ?: "Perro"
            breed = selectedPet?.breed.orEmpty()
            sex = selectedPet?.sex ?: "Macho"
            age = selectedPet?.age?.toString().orEmpty()
            color = selectedPet?.color.orEmpty()
            size = selectedPet?.size ?: "Mediano"
            hasMicrochip = selectedPet?.hasMicrochip ?: false
            isNeutered = selectedPet?.isNeutered ?: false
            ownerId = selectedPet?.ownerId.orEmpty()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminMascotasUiState.Error -> {
                message = (uiState as AdminMascotasUiState.Error).message
                saving = false
            }
            is AdminMascotasUiState.Success -> {
                if (saving) {
                    saving = false
                    onNavigateBack()
                }
            }
            else -> {}
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
                    text = if (petId == null) "Nueva mascota" else "Editar mascota",
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
                selected = species,
                options = listOf("Perro", "Gato", "Otro"),
                onSelected = { species = it },
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
                selected = sex,
                options = listOf("Macho", "Hembra"),
                onSelected = { sex = it },
            )

            if (petId == null) {
                OutlinedTextField(
                    value = ownerId,
                    onValueChange = { ownerId = it },
                    label = { Text("Dueño ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
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
                selected = size,
                options = listOf("Pequeño", "Mediano", "Grande"),
                onSelected = { size = it },
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
                    color = if (uiState is AdminMascotasUiState.Error) Color(0xFFD32F2F) else TextAccent,
                )
            }

            when (uiState) {
                is AdminMascotasUiState.Loading -> CircularProgressIndicator()
                else -> PrimaryButton(
                    text = "Guardar",
                    onClick = {
                        message = ""
                        saving = true
                        val request = PetRequest(
                            name = name.trim(),
                            species = species.lowercase(),
                            breed = breed.trim(),
                            sex = sex,
                            age = age.toIntOrNull() ?: 0,
                            color = color.trim(),
                            size = size,
                            hasMicrochip = hasMicrochip,
                            isNeutered = isNeutered,
                            photoBase64 = photoBase64,
                            ownerId = if (petId == null) ownerId.trim() else ownerId,
                        )
                        if (petId == null) {
                            viewModel.createPet(request)
                        } else {
                            viewModel.updatePet(petId, request)
                        }
                    },
                )
            }

            PrimaryButton(text = "Volver", onClick = onNavigateBack)
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
                .fillMaxSize()
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
