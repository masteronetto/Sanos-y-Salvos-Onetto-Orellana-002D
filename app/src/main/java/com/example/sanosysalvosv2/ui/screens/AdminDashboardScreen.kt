package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.viewmodel.AdminViewModel

private val DashboardGreen = Color(0xFF0E5B3D)
private val DashboardTeal = Color(0xFF0F8A8A)
private val DashboardBorder = Color(0xFFD7E5E3)
private val DashboardMuted = Color(0xFF7A7A7A)

@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = { AdminDashboardTopBar(onLogout = onLogout) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Panel de administración",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = DashboardGreen,
            )
            Text(
                text = "Resumen general del estado de la plataforma",
                color = DashboardMuted,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricCard("Usuarios activos", "1.248", Modifier.weight(1f))
                MetricCard("Reportes abiertos", "94", Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricCard("Coincidencias hoy", "27", Modifier.weight(1f))
                MetricCard("Entidades", "42", Modifier.weight(1f))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Actividad semanal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    SimpleBar("Lun", 0.45f)
                    SimpleBar("Mar", 0.62f)
                    SimpleBar("Mié", 0.58f)
                    SimpleBar("Jue", 0.71f)
                    SimpleBar("Vie", 0.81f)
                    SimpleBar("Sáb", 0.39f)
                    SimpleBar("Dom", 0.33f)
                }
            }

            if (adminViewModel.loading) {
                Text(text = "Cargando datos...", color = DashboardMuted)
            }
            adminViewModel.error?.let { message ->
                Text(text = message, color = Color(0xFFC53B3B))
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, color = DashboardMuted)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = DashboardGreen,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SimpleBar(
    label: String,
    ratio: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = label, color = DashboardMuted, modifier = Modifier.width(36.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .background(Color(0xFFF3F7F6), RoundedCornerShape(10.dp))
                .border(1.dp, DashboardBorder, RoundedCornerShape(10.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .height(16.dp)
                    .background(DashboardTeal, RoundedCornerShape(10.dp)),
            )
        }
    }
}

@Composable
private fun AdminDashboardTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onLogout,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DashboardTeal),
        ) {
            Text(text = "Salir", color = Color.White)
        }
    }
}
