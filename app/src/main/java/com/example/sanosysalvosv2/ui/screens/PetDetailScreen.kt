package com.example.sanosysalvosv2.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.util.TranslationUtils
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
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Hero photo at top
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
                    contentDescription = "Foto de ${pet.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF4A9B8E).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFF4A9B8E).copy(alpha = 0.4f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // Content below photo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name + sterilized badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pet.name.replaceFirstChar { it.uppercase() },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D6A5F)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (pet.hasMicrochip) 
                        Color(0xFF4A9B8E).copy(alpha = 0.15f)
                    else Color.Gray.copy(alpha = 0.1f)
                ) {
                    Text(
                        if (pet.hasMicrochip) "Con microchip" else "Sin microchip",
                        color = if (pet.hasMicrochip) Color(0xFF4A9B8E) else Color.Gray,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }
            
            // Species · Breed
            Text(
                "${TranslationUtils.species(pet.species)} · ${pet.breed.replaceFirstChar { it.uppercase() }}",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Divider(color = Color(0xFF4A9B8E).copy(alpha = 0.2f))
            
            // Info grid
            InfoRow("Sexo", TranslationUtils.gender(pet.sex))
            InfoRow("Edad", if (pet.age > 0) "${pet.age} año${if (pet.age != 1) "s" else ""}" else "No especificada")
            InfoRow("Color", pet.color.replaceFirstChar { it.uppercase() })
            InfoRow("Tamaño", TranslationUtils.size(pet.size))
            InfoRow("Esterilizado", if (pet.isNeutered) "Sí" else "No")
            
            Divider(color = Color(0xFF4A9B8E).copy(alpha = 0.2f))
            
            // Action buttons
            Button(
                onClick = { onNavigateToEditPet(pet.id) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A9B8E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Editar mascota", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onNavigateToReportPet(pet.id) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reportar perdida", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(
            value, 
            fontWeight = FontWeight.Medium, 
            fontSize = 14.sp,
            color = Color(0xFF2D2D2D)
        )
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
