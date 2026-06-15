package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.PetsUiState
import com.example.sanosysalvosv2.viewmodel.PetsViewModel

@Composable
fun PetDetailScreen(
    petId: String,
    petsViewModel: PetsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToReportPet: (String) -> Unit,
) {
    val selectedPet by petsViewModel.selectedPet.collectAsState()
    val uiState by petsViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(petId) {
        petsViewModel.loadPetDetails(petId)
    }

    when {
        uiState is PetsUiState.Loading || selectedPet == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        uiState is PetsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (uiState as PetsUiState.Error).message,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        selectedPet != null -> {
            PetDetailContent(
                pet = selectedPet!!,
                onNavigateBack = onNavigateBack,
                onNavigateToEditPet = onNavigateToEditPet,
                onNavigateToReportPet = onNavigateToReportPet,
                scrollState = scrollState,
            )
        }
    }
}

@Composable
private fun PetDetailContent(
    pet: PetResponse,
    onNavigateBack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToReportPet: (String) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp),
                tint = TextSecondary,
            )
            Text(
                text = "Detalles de mascota",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDEDED)),
                contentAlignment = Alignment.Center,
            ) {
                PetPhoto(
                    photoBase64 = pet.photoBase64,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            // Pet name
            Text(
                text = pet.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            // Status chip
            Box(
                modifier = Modifier
                    .background(TextAccent.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Activo",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info grid (2 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoItem(label = "Especie", value = pet.species)
                    InfoItem(label = "Raza", value = pet.breed)
                    InfoItem(label = "Sexo", value = pet.sex)
                    InfoItem(label = "Edad", value = "${pet.age} años")
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoItem(label = "Color", value = pet.color)
                    InfoItem(label = "Microchip", value = if (pet.id.isNotEmpty()) "Sí" else "No")
                    InfoItem(label = "Esterilizado", value = if (pet.isNeutered) "Sí" else "No")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Owner section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dueño",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                Text(
                    text = pet.ownerId.ifEmpty { "Sin información" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryButton(
                text = "Editar",
                onClick = { onNavigateToEditPet(pet.id) },
            )
            PrimaryButton(
                text = "Reportar como perdida",
                onClick = { onNavigateToReportPet(pet.id) },
            )
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
