package com.example.sanosysalvosv2.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanosysalvosv2.model.CollaboratorResponse

@Composable
fun CollaboratorDetailScreen(
    collaborator: CollaboratorResponse,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    val typeLabel = when {
        collaborator.type.uppercase().contains("VETER") || collaborator.type.uppercase().contains("CLINIC") -> "Clínica Veterinaria"
        collaborator.type.uppercase().contains("SHELTER") || collaborator.type.uppercase().contains("REFUGIO") -> "Refugio de Animales"
        collaborator.type.uppercase().contains("MUNIC") -> "Municipalidad"
        else -> collaborator.type
    }

    val badgeColor = when (typeLabel.split(" ").first()) {
        "Clínica" -> Color(0xFF4A9B8E)
        "Refugio" -> Color(0xFFFF8C00)
        "Municipalidad" -> Color(0xFF1976D2)
        else -> Color(0xFF4A9B8E)
    }

    val statusBadgeColor = when (collaborator.status.uppercase()) {
        "ACTIVE" -> Color(0xFF4CAF50)
        "INACTIVE" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState),
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = "Detalles de Entidad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        Divider(modifier = Modifier.fillMaxWidth())

        // Avatar / Icon
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp)
                .size(96.dp)
                .background(badgeColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = typeLabel.first().toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
            )
        }

        // Name
        Text(
            text = collaborator.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        )

        // Type badge
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
                .background(badgeColor.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = typeLabel,
                color = badgeColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Status badge
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
                .background(statusBadgeColor.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Estado: ${collaborator.status}",
                color = statusBadgeColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contact Information Section
        Text(
            text = "Información de Contacto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email
        if (collaborator.email.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Color(0xFF4A9B8E),
                    modifier = Modifier.size(24.dp),
                )
                Column {
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )
                    Text(
                        text = collaborator.email,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Phone
        if (collaborator.phone.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    tint = Color(0xFF4A9B8E),
                    modifier = Modifier.size(24.dp),
                )
                Column {
                    Text(
                        text = "Teléfono",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )
                    Text(
                        text = collaborator.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Location Information Section
        Text(
            text = "Información de Ubicación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Comuna
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocationCity,
                contentDescription = null,
                tint = Color(0xFF4A9B8E),
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text(
                    text = "Comuna",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = collaborator.comuna,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Address
        if (collaborator.address?.isNotBlank() == true) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationCity,
                    contentDescription = null,
                    tint = Color(0xFF4A9B8E),
                    modifier = Modifier.size(24.dp),
                )
                Column {
                    Text(
                        text = "Dirección",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )
                    Text(
                        text = collaborator.address,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Button(
            onClick = { /* TODO: Call phone */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9B8E)),
        ) {
            Text("Contactar por teléfono", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* TODO: Send email */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9B8E)),
        ) {
            Text("Enviar correo electrónico", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
