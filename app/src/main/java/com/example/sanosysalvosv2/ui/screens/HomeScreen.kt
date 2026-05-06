package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.viewmodel.MapViewModel
import com.example.sanosysalvosv2.ui.components.PrimaryButton

@Composable
fun HomeScreen(
    mapViewModel: MapViewModel,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        mapViewModel.refreshMapData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text(text = "Sanos y Salvos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Sesion activa")

        Spacer(modifier = Modifier.height(10.dp))

        mapViewModel.provider?.let {
            Text(text = "Proveedor: ${it.provider}")
        }
        Text(text = "Capas disponibles: ${mapViewModel.layers.size}")
        Text(text = "Reportes cercanos: ${mapViewModel.markers.size}")

        mapViewModel.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }

        if (mapViewModel.loading) {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(text = "Refrescar mapa", onClick = { mapViewModel.refreshMapData() })
            }
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(text = "Cerrar sesion", onClick = onLogout)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(mapViewModel.markers.take(20)) { marker ->
                Text(
                    text = "${marker.title} (${marker.reportType}) - ${marker.distanceMeters.toInt()} m",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
