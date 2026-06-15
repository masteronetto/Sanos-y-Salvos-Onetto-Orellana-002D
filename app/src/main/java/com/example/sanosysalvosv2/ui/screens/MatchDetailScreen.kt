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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sanosysalvosv2.viewmodel.UserMatchesViewModel
import com.example.sanosysalvosv2.viewmodel.MatchesUiState

private val Teal = Color(0xFF0F8A8A)
private val TealSoft = Color(0xFFEAF7F6)
private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val BorderColor = Color(0xFFD7E5E3)
private val RejectedRed = Color(0xFFC53B3B)

@Composable
fun MatchDetailScreen(
    matchId: String,
    onNavigateBack: () -> Unit,
) {
    val contextApp = LocalContext.current.applicationContext as Application
    val viewModel = remember(contextApp) { UserMatchesViewModel(contextApp) }
    val selectedMatch by viewModel.selectedMatch.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(matchId) {
        if (matchId.isNotBlank()) {
            viewModel.loadMatchDetails(matchId)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is MatchesUiState.ActionDone) {
            snackbarHostState.showSnackbar("Acción realizada")
            onNavigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateBack() },
                    tint = DarkGreen,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Detalle de coincidencia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                )
            }

            if (selectedMatch != null) {
                val match = selectedMatch!!
                val percentage = match.score
                val level = when {
                    percentage >= 80 -> "Alta"
                    percentage >= 50 -> "Media"
                    else -> "Baja"
                }
                val levelColor = when {
                    percentage >= 80 -> Color(0xFF4CAF50)
                    percentage >= 50 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }

                // Percentage badge and level chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Percentage badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(TealSoft, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Teal,
                        )
                    }

                    // Level chip
                    Box(
                        modifier = Modifier
                            .background(levelColor.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = level,
                            style = MaterialTheme.typography.labelMedium,
                            color = levelColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Show lost/found report ids
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Lost report ID: ${match.lostReportId}")
                    Text(text = "Found report ID: ${match.foundReportId}")
                }

                // Similarity details section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TealSoft, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Detalles de similitud",
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                    )
                    match.reason?.split("\n")?.forEach { line ->
                        if (line.contains(":")) {
                            val parts = line.split(":")
                            if (parts.size == 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = parts[0].trim(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GrayText,
                                    )
                                    Text(
                                        text = parts[1].trim(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Teal,
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom buttons (only for PENDING)
                if (match.status == "PENDING") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.rejectMatch(match.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Rechazar", color = RejectedRed)
                        }
                        Button(
                            onClick = { viewModel.acceptMatch(match.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        ) {
                            Text("Confirmar coincidencia", color = Color.White)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun MatchPetCard(
    title: String,
    name: String,
    date: String,
    comuna: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = GrayText,
                fontWeight = FontWeight.SemiBold,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(TealSoft, RoundedCornerShape(8.dp)),
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
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = GrayText,
            )

            Text(
                text = comuna,
                style = MaterialTheme.typography.labelSmall,
                color = GrayText,
            )
        }
    }
}
