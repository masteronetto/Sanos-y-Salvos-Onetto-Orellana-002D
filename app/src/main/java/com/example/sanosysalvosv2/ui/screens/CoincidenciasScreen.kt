package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import android.app.Application
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.sanosysalvosv2.viewmodel.MatchesUiState
import com.example.sanosysalvosv2.viewmodel.UserMatchesViewModel

private data class MatchCard(
    val matchPercent: Int,
    val lostPetName: String,
    val lostDate: String,
    val lostComuna: String,
    val foundDate: String,
    val foundComuna: String,
)

@Composable
fun CoincidenciasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMatchDetail: (String) -> Unit = {},
    onContactar: (String) -> Unit = {},
) {
    val contextApp = LocalContext.current.applicationContext as Application
    val viewModel = remember { UserMatchesViewModel(contextApp) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyMatches() }

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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() },
            )
            Text(
                text = "Coincidencias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        when (uiState) {
            is MatchesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            is MatchesUiState.Error -> {
                val msg = (uiState as MatchesUiState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = msg)
                }
            }
            is MatchesUiState.Success -> {
                val matches = (uiState as MatchesUiState.Success).matches
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(matches) { m ->
                        MatchCardItem(
                            match = MatchCard(
                                matchPercent = m.score,
                                lostPetName = m.lostReportId,
                                lostDate = m.createdAt?.let { java.time.Instant.ofEpochMilli(it).toString() } ?: "",
                                lostComuna = "",
                                foundDate = "",
                                foundComuna = "",
                            ),
                            status = m.status,
                            reason = m.reason,
                            onVerDetalles = { onNavigateToMatchDetail(m.id) },
                            onConfirm = { viewModel.acceptMatch(m.id) },
                            onReject = { viewModel.rejectMatch(m.id) },
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun MatchCardItem(
    match: MatchCard,
    status: String,
    reason: String,
    onVerDetalles: () -> Unit,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
) {
    val labelText: String
    val labelBg: Color
    val labelFg: Color

    when {
        match.matchPercent >= 85 -> {
            labelText = "Coincidencia alta"
            labelBg = TextAccent.copy(alpha = 0.15f)
            labelFg = TextAccent
        }
        match.matchPercent >= 70 -> {
            labelText = "Coincidencia media"
            labelBg = Color(0xFFFFE9CC)
            labelFg = Color(0xFFB26A00)
        }
        else -> {
            labelText = "Coincidencia baja"
            labelBg = Color(0xFFFFE5E5)
            labelFg = Color(0xFFC62828)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Borders, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(TextAccent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "${match.matchPercent}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }

            Box(
                modifier = Modifier
                    .background(labelBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelFg,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PetColumn(
                name = match.lostPetName,
                statusLabel = "(Perdida)",
                statusColor = Color(0xFFC62828),
                date = match.lostDate,
                comuna = match.lostComuna,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(120.dp)
                    .background(Color(0xFFEEEEEE)),
            )

            PetColumn(
                name = "Mascota",
                statusLabel = "Encontrada",
                statusColor = TextAccent,
                date = match.foundDate,
                comuna = match.foundComuna,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onVerDetalles,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextAccent),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextAccent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Ver detalles", fontWeight = FontWeight.SemiBold)
            }

            if (status == "PENDING") {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                ) {
                    Text(text = "Rechazar", color = Color.Red)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TextAccent),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = "Confirmar", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = { /* contact or noop */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TextAccent),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = "Contactar", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PetColumn(
    name: String,
    statusLabel: String,
    statusColor: Color,
    date: String,
    comuna: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFEDEDED), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = "Foto mascota",
                tint = TextSecondary,
                modifier = Modifier.size(36.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Text(
            text = comuna,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}
