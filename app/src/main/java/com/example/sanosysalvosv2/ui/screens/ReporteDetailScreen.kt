package com.example.sanosysalvosv2.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.util.TranslationUtils
import com.example.sanosysalvosv2.viewmodel.UserReportsUiState
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel

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
    reportViewModel: UserReportsViewModel,
    reportId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToEditReport: (String) -> Unit = {},
) {
    val uiState by reportViewModel.uiState.collectAsState()
    val selectedReport by reportViewModel.selectedReport.collectAsState()

    LaunchedEffect(reportId) {
        reportViewModel.loadReportDetails(reportId)
    }

    if (selectedReport == null && uiState is UserReportsUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val reporte = selectedReport ?: return
    val isResolved = reporte.status?.equals("RESOLVED", ignoreCase = true) == true
    val reportTypeText = TranslationUtils.reportType(reporte.type)

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
            val photoBitmap = remember(reporte.photoBase64) {
                reporte.photoBase64?.let { rawPhoto ->
                    try {
                        val base64 = rawPhoto.substringAfter("base64,", rawPhoto)
                        val bytes = Base64.decode(base64, Base64.NO_WRAP)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (photoBitmap != null) {
                Image(
                    bitmap = photoBitmap.asImageBitmap(),
                    contentDescription = "Foto del reporte",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            } else {
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
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(if (reportTypeText == "Perdida") Color(0xFFC62828) else TextAccent, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = reportTypeText,
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
            val petDisplayName = if (reporte.species != null) {
                "${TranslationUtils.species(reporte.species)}${if (!reporte.breed.isNullOrBlank()) " (${reporte.breed})" else ""}"
            } else {
                (reporte.breed ?: reporte.locationName).orEmpty().ifBlank { "Reporte sin nombre" }
            }

            Text(
                text = petDisplayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            SectionTitle("Descripción")
            Text(
                text = reporte.description.orEmpty().ifBlank { "No hay descripción disponible." },
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
                ReporteInfoRow("Tipo", reportTypeText)
                ReporteInfoRow("Estado", TranslationUtils.reportStatus(reporte.status))
                val reporterName = reporte.reporterName?.takeIf { it.isNotBlank() } ?: reporte.reporterId
                val reporterPhone = reporte.reporterPhone?.takeIf { it.isNotBlank() }

                ReporteInfoRow("Reportado por", reporterName.orEmpty().ifBlank { "Desconocido" })
                ReporteInfoRow("Teléfono", reporterPhone.orEmpty().ifBlank { "No disponible" })
                ReporteInfoRow("Comuna", reporte.locationName.orEmpty().ifBlank { "No disponible" })
                ReporteInfoRow("Fecha", (reporte.eventDate ?: reporte.createdAt).orEmpty().ifBlank { "No disponible" })
                ReporteInfoRow("Ubicación", reporte.locationName.orEmpty().ifBlank { "No disponible" })
                
                // Show updatedAt if available and different from createdAt
                if (!reporte.updatedAt.isNullOrBlank() && reporte.updatedAt != reporte.createdAt) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Actualizado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                        )
                        Text(
                            reporte.updatedAt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { onNavigateToEditReport(reporte.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextAccent),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = "Editar reporte", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { reportViewModel.markAsResolved(reporte.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TextAccent),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isResolved,
                ) {
                    Text(
                        text = if (isResolved) "Reporte resuelto" else "Marcar como resuelto",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
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
