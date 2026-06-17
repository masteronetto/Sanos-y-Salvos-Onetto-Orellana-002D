package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import android.app.Application
import com.example.sanosysalvosv2.viewmodel.AdminReportsViewModel
import com.example.sanosysalvosv2.util.TranslationUtils
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val Teal = Color(0xFF0F8A8A)
private val Red = Color(0xFFC53B3B)
private val BorderColor = Color(0xFFD7E5E3)
private val GrayText = Color(0xFF7A7A7A)
private val DarkGreen = Color(0xFF0E5B3D)

private data class ReportDetailMock(
    val id: String,
    val petName: String,
    val status: String,
    val species: String,
    val breed: String,
    val sex: String,
    val age: String,
    val color: String,
    val particularSigns: String,
    val reporter: String,
    val phone: String,
    val comuna: String,
    val lostDate: String,
    val lastLocation: String,
)

private val mockReport = ReportDetailMock(
    id = "R001",
    petName = "Perla",
    status = "Perdida",
    species = "Perro",
    breed = "Pomerania Toy",
    sex = "Hembra",
    age = "3 años",
    color = "Blanco",
    particularSigns = "Pequeña cicatriz en la oreja derecha. Lleva collar rosa con placa azul.",
    reporter = "Camila Orellana",
    phone = "+56 9 7123 4567",
    comuna = "Maipú",
    lostDate = "02/06/2026",
    lastLocation = "Parque El Retiro, Maipú",
)

@Composable
fun AdminReporteDetailScreen(
    reportId: String,
    onNavigateBack: () -> Unit,
) {
    val contextApp = LocalContext.current.applicationContext as Application
    val vm = remember(contextApp) { AdminReportsViewModel(contextApp) }
    val selected by vm.selectedReport.collectAsState()

    LaunchedEffect(reportId) {
        if (reportId.isNotBlank()) vm.loadReportDetails(reportId)
    }

    val report = selected?.let {
        ReportDetailMock(
            id = it.id,
            petName = if (it.species != null) TranslationUtils.species(it.species) else (it.breed ?: it.locationName ?: "-"),
            status = it.status ?: "-",
            species = TranslationUtils.species(it.species),
            breed = it.breed ?: "-",
            sex = "-",
            age = "-",
            color = it.color ?: "-",
            particularSigns = it.description ?: "-",
            reporter = it.reporterId ?: "-",
            phone = "-",
            comuna = it.locationName ?: "-",
            lostDate = it.eventDate ?: it.createdAt ?: "-",
            lastLocation = it.locationName ?: "-",
        )
    } ?: mockReport.copy(id = reportId.ifBlank { mockReport.id })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onNavigateBack() },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DarkGreen,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "← Volver a reportes",
                    color = DarkGreen,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "Gestión de reportes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFFF2F2F2), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            selected?.photoUrl?.takeIf { it.isNotBlank() }?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } ?: Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Filled.Pets, contentDescription = null, tint = GrayText, modifier = Modifier.size(80.dp))
                Text(text = "Hero image", color = GrayText)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ThumbnailBox(label = "1", modifier = Modifier.weight(1f))
            ThumbnailBox(label = "2", modifier = Modifier.weight(1f))
            ThumbnailBox(label = "3", modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(82.dp)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Teal)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = report.petName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
            StatusBadge(text = TranslationUtils.reportStatus(report.status), color = TranslationUtils.statusColor(report.status))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRow(label = "Nombre mascota", value = report.petName)
                InfoRow(label = "Reportado por", value = report.reporter)
                InfoRow(label = "Comuna", value = report.comuna)
                InfoRow(label = "Fecha", value = report.lostDate)
                InfoRow(label = "Ubicación", value = report.lastLocation)
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Descripción", fontWeight = FontWeight.Bold)
                    Text(text = report.particularSigns, color = GrayText)
                }
            }
        }

        Text(
            text = "Información del reporte",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailKeyValue(label = "ID", value = report.id)
                DetailKeyValue(label = "Reportado por", value = report.reporter)
                DetailKeyValue(label = "Telefono", value = report.phone)
                DetailKeyValue(label = "Comuna", value = report.comuna)
                DetailKeyValue(label = "Fecha de pérdida", value = report.lostDate)
                DetailKeyValue(label = "Última ubicación", value = report.lastLocation)
                DetailKeyValue(label = "Estado", value = TranslationUtils.reportStatus(report.status), valueColor = TranslationUtils.statusColor(report.status))
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { vm.rejectReport(report.id); onNavigateBack() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Red),
            ) {
                Text(text = "Rechaza reporte", color = Red, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { vm.approveReport(report.id); onNavigateBack() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
            ) {
                Text(text = "Aprobar reporte", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ThumbnailBox(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F7F7), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = GrayText, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    val background = color.copy(alpha = 0.12f)
    val foreground = color

    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .border(1.dp, foreground.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text = text, color = foreground, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = GrayText, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailKeyValue(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(text = label, color = GrayText, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}