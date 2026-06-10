package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary

private enum class NotifType {
    COINCIDENCIA,
    REPORTE,
    SISTEMA,
}

private enum class NotifFilter(val label: String) {
    TODAS("Todas"),
    COINCIDENCIAS("Coincidencias"),
    REPORTES("Reportes"),
    SISTEMA("Sistema"),
}

private data class Notificacion(
    val title: String,
    val subtitle: String,
    val relativeTime: String,
    val type: NotifType,
)

@Composable
fun NotificacionesScreen(
    onNavigateBack: () -> Unit,
) {
    val allNotifications = listOf(
        Notificacion(
            title = "Nueva coincidencia encontrada",
            subtitle = "Una perrita fue hallada",
            relativeTime = "Hace 15 minutos",
            type = NotifType.COINCIDENCIA,
        ),
        Notificacion(
            title = "Nueva respuesta en tu reporte",
            subtitle = "Alguien encontró un perro similar",
            relativeTime = "Hace 1 hora",
            type = NotifType.REPORTE,
        ),
        Notificacion(
            title = "Reporte marcado como resuelto",
            subtitle = "Alguien encontró un perro similar",
            relativeTime = "Hace 1 hora",
            type = NotifType.SISTEMA,
        ),
        Notificacion(
            title = "Gracias por tu colaboración",
            subtitle = "Tu reporte encontrado fue útil",
            relativeTime = "Hace 1 hora",
            type = NotifType.REPORTE,
        ),
    )

    var selectedFilter by remember { mutableStateOf(NotifFilter.TODAS) }

    val filtered = when (selectedFilter) {
        NotifFilter.TODAS -> allNotifications
        NotifFilter.COINCIDENCIAS -> allNotifications.filter { it.type == NotifType.COINCIDENCIA }
        NotifFilter.REPORTES -> allNotifications.filter { it.type == NotifType.REPORTE }
        NotifFilter.SISTEMA -> allNotifications.filter { it.type == NotifType.SISTEMA }
    }

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
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() },
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(modifier = Modifier.size(24.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NotifFilter.entries.forEach { filter ->
                FilterTab(
                    label = filter.label,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filtered) { notif ->
                NotifCard(notif = notif)
                Divider(
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp,
                )
            }
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) TextAccent else TextSecondary,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(TextAccent, RoundedCornerShape(1.dp)),
            )
        }
    }
}

@Composable
private fun NotifCard(notif: Notificacion) {
    val icon: ImageVector
    val iconTint: Color

    when (notif.type) {
        NotifType.COINCIDENCIA -> {
            icon = Icons.Filled.Warning
            iconTint = Color(0xFFE65100)
        }
        NotifType.REPORTE -> {
            icon = Icons.Filled.Campaign
            iconTint = TextAccent
        }
        NotifType.SISTEMA -> {
            icon = Icons.Filled.CheckCircle
            iconTint = Color(0xFF2E7D32)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = notif.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = notif.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = notif.relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFAAAAAA),
            )
        }
    }
}
