package com.example.sanosysalvosv2.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.util.TranslationUtils
import com.example.sanosysalvosv2.viewmodel.PetsUiState
import com.example.sanosysalvosv2.viewmodel.PetsViewModel

@Composable
fun MascotasScreen(
    petsViewModel: PetsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit,
    onNavigateToEditPet: (String) -> Unit,
) {
    val uiState by petsViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                petsViewModel.loadMyPets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(pets) { pet ->
                            PetCard(
                                pet = pet,
                                onEditClick = { onNavigateToEditPet(pet.id) },
                                onDetailsClick = { onNavigateToPetDetail(pet.id) },
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
fun PetCard(
    pet: PetResponse,
    onEditClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFF4A9B8E).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo
            if (pet.photoBase64.isNotEmpty()) {
                val bitmap = remember(pet.photoBase64) {
                    try {
                        val bytes = Base64.decode(pet.photoBase64, Base64.NO_WRAP)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = pet.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PetPhotoPlaceholder(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)))
                }
            } else {
                PetPhotoPlaceholder(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)))
            }

            // Name + species + buttons
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pet.name.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2D6A5F)
                )
                Text(
                    text = TranslationUtils.species(pet.species),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Editar",
                            color = Color(0xFF4A9B8E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(
                        onClick = onDetailsClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Ver detalles",
                            color = Color(0xFF4A9B8E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetPhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF4A9B8E).copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Pets,
            contentDescription = null,
            tint = Color(0xFF4A9B8E).copy(alpha = 0.4f),
            modifier = Modifier.size(32.dp)
        )
    }
}
