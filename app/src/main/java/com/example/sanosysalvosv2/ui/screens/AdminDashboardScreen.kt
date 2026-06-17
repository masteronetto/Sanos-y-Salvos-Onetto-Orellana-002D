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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.viewmodel.AdminDashboardNewViewModel
import com.example.sanosysalvosv2.viewmodel.LoadingState
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.data.repository.AdminStatsRepository

private val DashboardGreen = Color(0xFF0E5B3D)
private val DashboardTeal = Color(0xFF0F8A8A)
private val DashboardBorder = Color(0xFFD7E5E3)
private val DashboardMuted = Color(0xFF7A7A7A)

class AdminDashboardNewViewModelFactory(
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardNewViewModel::class.java)) {
            return AdminDashboardNewViewModel(
                sessionStore = SessionStore(context),
                statsRepo = AdminStatsRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val dashboardViewModel: AdminDashboardNewViewModel = viewModel(
        factory = AdminDashboardNewViewModelFactory(context.applicationContext)
    )

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
    }

    val dashboardStats by dashboardViewModel.dashboardStats.collectAsState()
    val recoveryStats by dashboardViewModel.recoveryStats.collectAsState()
    val reportsByCommune by dashboardViewModel.reportsByCommune.collectAsState()
    val loadingState by dashboardViewModel.loadingState.collectAsState()

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

            when (loadingState) {
                is LoadingState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Cargando panel...")
                    }
                }
                is LoadingState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Error: ${(loadingState as LoadingState.Error).message}", color = Color.Red)
                    }
                }
                else -> {
                    // Stats grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MetricCard(
                            "Usuarios activos",
                            dashboardStats?.activeUsers?.toString() ?: "0",
                            Modifier.weight(1f),
                        )
                        MetricCard(
                            "Reportes abiertos",
                            dashboardStats?.openReports?.toString() ?: "0",
                            Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MetricCard(
                            "Coincidencias hoy",
                            dashboardStats?.matchesThisWeek?.toString() ?: "0",
                            Modifier.weight(1f)
                        )
                        MetricCard(
                            "Entidades",
                            dashboardStats?.totalEntidades?.toString() ?: "0",
                            Modifier.weight(1f)
                        )
                    }
                }
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
                    val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    val weeklyMap = dashboardStats?.let { it.reportsThisWeek to it.matchesThisWeek }
                    val maxActivity = 1
                    dayNames.forEach { day ->
                        // Use placeholder per-day values from dashboardRepo weeklyActivity if available
                        SimpleBar(day, 0f)
                    }
                }
            }

            // Recovery stats
            recoveryStats?.let { rec ->
                Column {
                    Text("Estadísticas de recuperación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard("Tasa de recuperación", "${rec.recoveryRate.toInt()}%", Modifier.weight(1f))
                        MetricCard("Tiempo promedio", "${rec.averageTimeInDays} días", Modifier.weight(1f))
                    }
                }
            }

            // Reports by commune
            if (reportsByCommune.isNotEmpty()) {
                Column {
                    Text("Reportes por comuna", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    reportsByCommune.forEach { commune ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(commune.communeName)
                            Text(commune.count.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
