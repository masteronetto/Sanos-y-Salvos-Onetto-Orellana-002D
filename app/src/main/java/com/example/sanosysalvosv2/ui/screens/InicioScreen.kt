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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary

private data class SummaryStat(
    val title: String,
    val count: Int,
)

private data class NotificationItem(
    val title: String,
    val subtitle: String,
    val relativeTime: String,
    val type: NotificationType,
)

private enum class NotificationType {
    MATCH,
    REPORT,
}

@Composable
fun InicioScreen(
    userName: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAllNotifications: () -> Unit,
) {
    val stats = listOf(
        SummaryStat(title = "Mis mascotas", count = 3),
        SummaryStat(title = "Perdidas", count = 1),
        SummaryStat(title = "Encontradas", count = 2),
        SummaryStat(title = "Coincidencias", count = 4),
    )

    val notifications = listOf(
        NotificationItem(
            title = "Nueva coincidencia detectada",
            subtitle = "Luna coincide con un reporte cercano.",
            relativeTime = "Hace 15 minutos",
            type = NotificationType.MATCH,
        ),
        NotificationItem(
            title = "Reporte comunitario",
            subtitle = "Vieron una mascota similar en Parque Central.",
            relativeTime = "Hace 1 hora",
            type = NotificationType.REPORT,
        ),
    )

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
                        SummaryCard(stat = stats[0], modifier = Modifier.weight(1f))
                        SummaryCard(stat = stats[1], modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryCard(stat = stats[2], modifier = Modifier.weight(1f))
                        SummaryCard(stat = stats[3], modifier = Modifier.weight(1f))
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

                notifications.take(2).forEach { item ->
                    NotificationCard(item = item)
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
                    text = item.relativeTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
