package com.example.sanosysalvosv2.ui.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.sanosysalvosv2.viewmodel.AdminMatchesViewModel

private val Teal = Color(0xFF0F8A8A)
private val TealSoft = Color(0xFFEAF7F6)
private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val BorderColor = Color(0xFFD7E5E3)
private val RejectedRed = Color(0xFFC53B3B)

@Composable
fun AdminMatchDetailScreen(
    matchId: String,
    onNavigateBack: () -> Unit,
) {
    val contextApp = LocalContext.current.applicationContext as Application
    val ctx = LocalContext.current
    val viewModel = remember(contextApp) { AdminMatchesViewModel(contextApp) }
    val selectedMatch by viewModel.selectedMatch.collectAsState()

    LaunchedEffect(matchId) {
        if (matchId.isNotBlank()) {
            viewModel.loadMatchDetail(matchId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable { onNavigateBack() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Volver", color = DarkGreen, fontWeight = FontWeight.Medium)
            }

            Text(
                text = "Detalle de coincidencia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        selectedMatch?.let { match ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MatchPetCard(
                    title = "Mascota origen",
                    name = match.sourcePetName.orEmpty(),
                    details = listOf(
                        "Comuna: ${match.comuna.orEmpty()}",
                        "Puntaje: ${match.score ?: 0}%",
                        "Estado: ${match.status.orEmpty()}",
                        "Fecha: ${match.createdAt.orEmpty()}",
                    ),
                    imageUrl = match.sourcePetPhotoUrl,
                    modifier = Modifier.weight(1f),
                )

                MatchPetCard(
                    title = "Mascota encontrada",
                    name = match.matchedPetName.orEmpty(),
                    details = listOf(
                        "Comuna: ${match.comuna.orEmpty()}",
                        "Puntaje: ${match.score ?: 0}%",
                        "Estado: ${match.status.orEmpty()}",
                        "Fecha: ${match.createdAt.orEmpty()}",
                    ),
                    imageUrl = match.matchedPetPhotoUrl,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = match.details.orEmpty(),
                color = GrayText,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )

            // Reporter contact info (admin only)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3CD), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Reportado por:",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText,
                        )
                        Text(
                            text = "Contacto disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkGreen,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Llamar",
                        modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+56912345678"))
                                    ctx.startActivity(intent)
                                },
                        tint = Teal,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { viewModel.confirmMatch(match.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Confirmar")
                }
                OutlinedButton(
                    onClick = { viewModel.discardMatch(match.id) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = RejectedRed)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Descartar", color = RejectedRed)
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Cargando coincidencia...", color = GrayText)
        }
    }
}

@Composable
private fun MatchPetCard(
    title: String,
    name: String,
    details: List<String>,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = DarkGreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(TealSoft, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(imageVector = Icons.Filled.Pets, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                        Text("Sin imagen", color = GrayText, textAlign = TextAlign.Center)
                    }
                }
            }
            Text(text = name, fontWeight = FontWeight.Bold, color = Color.Black)
            details.forEach { detail ->
                Text(text = detail, color = GrayText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
