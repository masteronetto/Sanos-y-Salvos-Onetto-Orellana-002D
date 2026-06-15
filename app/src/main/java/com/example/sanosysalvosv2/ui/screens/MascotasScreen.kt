package com.example.sanosysalvosv2.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.PetsUiState
import com.example.sanosysalvosv2.viewmodel.PetsViewModel

private data class PetItem(
    val id: String,
    val name: String,
    val breed: String,
    val sex: String,
    val age: String,
    val sterilized: Boolean,
    val photoBase64: String,
)

@Composable
fun MascotasScreen(
    petsViewModel: PetsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit,
    onNavigateToEditPet: (String) -> Unit,
) {
    val uiState by petsViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        petsViewModel.loadMyPets()
    }

    LaunchedEffect(uiState) {
        if (uiState is PetsUiState.Saved || uiState is PetsUiState.Deleted) {
            petsViewModel.loadMyPets()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
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
                    .clickable { onNavigateBack() },
            )

            Text(
                text = "Mis Mascotas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(TextAccent, CircleShape)
                    .clickable { onNavigateToAddPet() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar mascota",
                    tint = Color.White,
                )
            }
        }

        when (uiState) {
            is PetsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is PetsUiState.Error -> {
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
            is PetsUiState.Success -> {
                val pets = (uiState as PetsUiState.Success).pets
                if (pets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Aún no tienes mascotas registradas.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(pets) { pet ->
                            PetCard(
                                pet = pet.toPetItem(),
                                onEdit = { onNavigateToEditPet(pet.id) },
                                onViewDetails = { onNavigateToPetDetail(pet.id) },
                            )
                        }
                    }
                }
            }
            else -> {
                // Idle state is handled by initial load effect.
            }
        }
    }
}

@Composable
private fun PetCard(
    pet: PetItem,
    onEdit: () -> Unit,
    onViewDetails: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Borders, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            PetPhoto(
                photoBase64 = pet.photoBase64,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pet.breed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = "${pet.sex} · ${pet.age}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )

                SterilizationChip(sterilized = pet.sterilized)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEdit) {
                        Text("Editar", color = TextAccent)
                    }
                    TextButton(onClick = onViewDetails) {
                        Text("Ver detalles", color = TextAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun PetPhoto(
    photoBase64: String,
    modifier: Modifier = Modifier,
) {
    if (photoBase64.isNotEmpty()) {
        val bitmap = remember(photoBase64) {
            try {
                val bytes = Base64.decode(photoBase64, Base64.NO_WRAP)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto de mascota",
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        } else {
            PetPhotoPlaceholder(modifier)
        }
    } else {
        PetPhotoPlaceholder(modifier)
    }
}

@Composable
fun PetPhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF4A9B8E).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Pets,
            contentDescription = null,
            tint = Color(0xFF4A9B8E).copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
fun SterilizationChip(sterilized: Boolean) {
    val text = if (sterilized) "Esterilizada" else "No esterilizada"
    Box(
        modifier = Modifier
            .background(TextAccent.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TextAccent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun AddPetScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "AddPetScreen (placeholder)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun PetDetailScreen(
    petViewModel: PetsViewModel,
    petId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
) {
    val selectedPet by petViewModel.selectedPet.collectAsState()
    val uiState by petViewModel.uiState.collectAsState()

    LaunchedEffect(petId) {
        if (petId.isNotBlank()) {
            petViewModel.loadPetDetails(petId)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        LaunchedEffect(uiState) {
            if (uiState is PetsUiState.Deleted) {
                onNavigateBack()
            }
        }

        when {
            uiState is PetsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            selectedPet != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                            text = "Detalles de la mascota",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    Text(
                        text = selectedPet!!.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${selectedPet!!.species.capitalize()} · ${selectedPet!!.breed}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )

                    Text(
                        text = "Sexo: ${selectedPet!!.sex}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Edad: ${selectedPet!!.age} años",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Color: ${selectedPet!!.color}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = if (selectedPet!!.isNeutered) "Esterilizado" else "No esterilizado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextAccent,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Editar mascota",
                        onClick = { onNavigateToEditPet(petId) },
                    )
                    TextButton(
                        onClick = { petViewModel.deletePet(petId) },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(
                            text = "Eliminar mascota",
                            color = Color(0xFFD32F2F),
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No se encontró la mascota.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

private fun PetResponse.toPetItem(): PetItem = PetItem(
    id = id,
    name = name,
    breed = breed,
    sex = sex,
    age = "$age años",
    sterilized = isNeutered,
    photoBase64 = photoBase64,
)
