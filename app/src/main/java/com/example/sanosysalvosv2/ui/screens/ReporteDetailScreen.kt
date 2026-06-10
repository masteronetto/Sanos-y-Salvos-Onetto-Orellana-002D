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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary

private enum class ReporteStatus { PENDIENTE, RESUELTO, RECHAZADO }
private enum class ReportePetStatus { PERDIDA, ENCONTRADA }

private data class ReporteDetalle(
    val id: String,
    val petName: String,
    val petStatus: ReportePetStatus,
    val especie: String,
    val raza: String,
    val sexo: String,
    val edad: String,
    val color: String,
    val seniasParticulares: String,
    val descripcion: String,
    val reportadoPor: String,
    val telefono: String,
    val comuna: String,
    val fechaPerdida: String,
    val ultimaUbicacion: String,
    val estado: ReporteStatus,
    val isOwner: Boolean,
)

private val mockReporte = ReporteDetalle(
    id = "RPT-001",
    petName = "Perla",
    petStatus = ReportePetStatus.PERDIDA,
    especie = "Perro",
    raza = "Mestiza",
    sexo = "Hembra",
    edad = "3 años",
    color = "Blanco y negro",
    seniasParticulares = "Mancha café en la oreja izquierda. Collar azul con placa.",
    descripcion = "Perla es una perra muy amigable y cariñosa. Se perdió cerca del parque el Retiro en Maipú. Responde a su nombre y le gustan los niños.",
    reportadoPor = "Camila Soto",
    telefono = "+56 9 8765 4321",
    comuna = "Maipú",
    fechaPerdida = "02/06/2026",
    ultimaUbicacion = "Parque El Retiro, Maipú",
    estado = ReporteStatus.PENDIENTE,
    isOwner = true,
)

@Composable
fun ReporteDetailScreen(
    reportId: String,
    onNavigateBack: () -> Unit = {},
) {
    val reporte = mockReporte

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable { onNavigateBack() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Volver a reportes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextAccent,
                )
            }
        }

        Text(
            text = "Detalle del reporte",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEDEDED), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = "Foto",
                    tint = TextSecondary,
                    modifier = Modifier.size(72.dp),
                )
            }

            val badgeBg = if (reporte.petStatus == ReportePetStatus.PERDIDA)
                Color(0xFFC62828) else TextAccent
            val badgeText = if (reporte.petStatus == ReportePetStatus.PERDIDA)
                "Perdida" else "Encontrada"

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(badgeBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = reporte.petName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoRow(label = "Especie", value = reporte.especie)
                    InfoRow(label = "Raza", value = reporte.raza)
                    InfoRow(label = "Sexo", value = reporte.sexo)
                    InfoRow(label = "Edad", value = reporte.edad)
                    InfoRow(label = "Color", value = reporte.color)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Borders, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Señales particulares",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = reporte.seniasParticulares,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            SectionTitle("Descripción")
            Text(
                text = reporte.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            SectionTitle("Información del reporte")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Borders, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReporteInfoRow("ID del reporte", reporte.id)
                ReporteInfoRow("Reportado por", reporte.reportadoPor)
                ReporteInfoRow("Teléfono", reporte.telefono)
                ReporteInfoRow("Comuna", reporte.comuna)
                ReporteInfoRow("Fecha de pérdida", reporte.fechaPerdida)
                ReporteInfoRow("Última ubicación", reporte.ultimaUbicacion)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Estado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    val (statusText, statusColor) = when (reporte.estado) {
                        ReporteStatus.PENDIENTE -> "Pendiente" to Color(0xFFB26A00)
                        ReporteStatus.RESUELTO -> "Resuelto" to TextAccent
                        ReporteStatus.RECHAZADO -> "Rechazado" to Color(0xFFC62828)
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                statusColor.copy(alpha = 0.15f),
                                RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (reporte.isOwner) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextAccent),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(text = "Editar reporte", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TextAccent),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "Marcar como resuelto",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ReporteInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
    }
}
