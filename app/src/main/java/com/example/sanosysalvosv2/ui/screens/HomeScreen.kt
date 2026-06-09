package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.viewmodel.MatchUiState
import com.example.sanosysalvosv2.viewmodel.MatchViewModel
import com.example.sanosysalvosv2.viewmodel.MapsUiState
import com.example.sanosysalvosv2.viewmodel.MapsViewModel

@Composable
fun HomeScreen(
    mapViewModel: MapsViewModel,
    matchViewModel: MatchViewModel,
    onLogout: () -> Unit,
    onNavigateToReportPet: () -> Unit,
) {
    val uiState by mapViewModel.uiState.collectAsState()
    val matchUiState by matchViewModel.uiState.collectAsState()
    val refreshLabel = when (uiState) {
        is MapsUiState.AwaitingLocation -> "Ubicando..."
        is MapsUiState.Loading -> "Cargando..."
        else -> "Refrescar mapa"
    }

    // Check for matches once on screen entry
    LaunchedEffect(Unit) {
        matchViewModel.checkForMatches()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapsScreen(viewModel = mapViewModel)

        LogoutTopBar(
            onLogout = onLogout,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
        )

        // Match notification banner
        if (matchUiState is MatchUiState.MatchesFound) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Posible coincidencia encontrada para tu mascota.",
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "✕",
                        onClick = { matchViewModel.dismissMatches() },
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PrimaryButton(
                        text = refreshLabel,
                        onClick = { mapViewModel.refreshLastKnownLocation() },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PrimaryButton(
                        text = "Reportar mascota",
                        onClick = onNavigateToReportPet,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutTopBar(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(text = "Sanos y Salvos")
            }
            Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(text = "Cerrar sesion", onClick = onLogout)
            }
        }
    }
}
