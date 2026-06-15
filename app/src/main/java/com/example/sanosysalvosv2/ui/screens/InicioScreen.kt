package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.InicioUiState
import com.example.sanosysalvosv2.viewmodel.InicioViewModel
import com.example.sanosysalvosv2.viewmodel.NotificationItem
import com.example.sanosysalvosv2.viewmodel.NotificationType

private data class SummaryStat(
    val title: String,
    val count: Int,
)

@Composable
fun InicioScreen(
    viewModel: InicioViewModel = viewModel(),
    onNavigateToNotifications: () -> Unit,
    onNavigateToAllNotifications: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = when (uiState) {
        is InicioUiState.Success -> (uiState as InicioUiState.Success).summary
        else -> null
    }

    val stats = summary?.let {
        listOf(
            SummaryStat(title = "Mis mascotas", count = it.myPetsCount),
            SummaryStat(title = "Perdidas", count = it.lostCount),
            SummaryStat(title = "Encontradas", count = it.foundCount),
            SummaryStat(title = "Coincidencias", count = it.matchesCount),
        )
    } ?: listOf(
        SummaryStat(title = "Mis mascotas", count = 0),
        SummaryStat(title = "Perdidas", count = 0),
        SummaryStat(title = "Encontradas", count = 0),
        SummaryStat(title = "Coincidencias", count = 0),
    )

    val notifications = summary?.recentNotifications ?: emptyList()
    val userName = summary?.userName ?: "Usuario"

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TextAccent)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "PetFind",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "¡Hola, $userName!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Bienvenida de vuelta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Resumen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notificaciones",
                            modifier = Modifier.clickable { onNavigateToNotifications() },
                            tint = TextAccent,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SummaryCard(stat = stats[0], modifier = Modifier.fillMaxWidth(0.5f))
                            SummaryCard(stat = stats[1], modifier = Modifier.fillMaxWidth(0.5f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SummaryCard(stat = stats[2], modifier = Modifier.fillMaxWidth(0.5f))
                            SummaryCard(stat = stats[3], modifier = Modifier.fillMaxWidth(0.5f))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Ver todos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextAccent,
                            modifier = Modifier.clickable { onNavigateToAllNotifications() },
                        )
                    }

                    if (uiState is InicioUiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = TextAccent)
                        }
                    } else if (uiState is InicioUiState.Error) {
                        val errorMessage = (uiState as InicioUiState.Error).message
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
                        )
                    } else {
                        notifications.take(2).forEach { item ->
                            NotificationCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    stat: SummaryStat,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .border(1.dp, Borders, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stat.title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = stat.count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationItem) {
    val icon = when (item.type) {
        NotificationType.MATCH -> Icons.Filled.NotificationsActive
        NotificationType.REPORT -> Icons.Filled.Campaign
        NotificationType.SYSTEM -> Icons.Filled.Notifications
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Borders, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextAccent,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = item.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
