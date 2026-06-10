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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary

private enum class ReportFilter {
    LOST,
    FOUND,
    ALL,
}

private enum class ReportType {
    LOST,
    FOUND,
}

private enum class ReportStatus {
    PENDING,
    RESOLVED,
}

private data class ReportItem(
    val id: String,
    val petName: String,
    val type: ReportType,
    val date: String,
    val comuna: String,
    val reporterName: String,
    val status: ReportStatus,
)

@Composable
fun ReportesScreen(
    onNavigateToNewReport: () -> Unit,
    onNavigateToReporteDetail: (String) -> Unit,
) {
    val mockReports = listOf(
        ReportItem(
            id = "rep-001",
            petName = "Perla",
            type = ReportType.LOST,
            date = "09 Jun 2026",
            comuna = "Maipu",
            reporterName = "Camila Soto",
            status = ReportStatus.PENDING,
        ),
        ReportItem(
            id = "rep-002",
            petName = "Masu",
            type = ReportType.FOUND,
            date = "08 Jun 2026",
            comuna = "Providencia",
            reporterName = "David Rojas",
            status = ReportStatus.RESOLVED,
        ),
        ReportItem(
            id = "rep-003",
            petName = "Luna",
            type = ReportType.LOST,
            date = "07 Jun 2026",
            comuna = "Santiago Centro",
            reporterName = "Fernanda Pizarro",
            status = ReportStatus.PENDING,
        ),
        ReportItem(
            id = "rep-004",
            petName = "Toby",
            type = ReportType.FOUND,
            date = "06 Jun 2026",
            comuna = "Nunoa",
            reporterName = "Matias Araya",
            status = ReportStatus.PENDING,
        ),
        ReportItem(
            id = "rep-005",
            petName = "Nina",
            type = ReportType.LOST,
            date = "05 Jun 2026",
            comuna = "Las Condes",
            reporterName = "Paula Diaz",
            status = ReportStatus.RESOLVED,
        ),
    )

    var selectedFilter by remember { mutableStateOf(ReportFilter.ALL) }

    val filteredReports = when (selectedFilter) {
        ReportFilter.LOST -> mockReports.filter { it.type == ReportType.LOST }
        ReportFilter.FOUND -> mockReports.filter { it.type == ReportType.FOUND }
        ReportFilter.ALL -> mockReports
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    label = "Perdidos",
                    selected = selectedFilter == ReportFilter.LOST,
                    onClick = { selectedFilter = ReportFilter.LOST },
                )
                FilterChip(
                    label = "Encontrados",
                    selected = selectedFilter == ReportFilter.FOUND,
                    onClick = { selectedFilter = ReportFilter.FOUND },
                )
                FilterChip(
                    label = "Todos",
                    selected = selectedFilter == ReportFilter.ALL,
                    onClick = { selectedFilter = ReportFilter.ALL },
                )
            }

            TextButton(onClick = onNavigateToNewReport) {
                Text(
                    text = "+ Nuevo reporte",
                    color = TextAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filteredReports) { report ->
                ReportCard(
                    report = report,
                    onClick = { onNavigateToReporteDetail(report.id) },
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) TextAccent else Color.White
    val textColor = if (selected) Color.White else TextAccent

    Surface(
        modifier = Modifier
            .border(1.dp, TextAccent, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ReportCard(
    report: ReportItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Borders, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = report.petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                ReportTypeBadge(type = report.type)
            }

            Text(
                text = "Fecha: ${report.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = "Comuna: ${report.comuna}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = "Reportado por: ${report.reporterName}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                ReportStatusChip(status = report.status)
            }
        }
    }
}

@Composable
private fun ReportTypeBadge(type: ReportType) {
    val text: String
    val bgColor: Color
    val fgColor: Color

    when (type) {
        ReportType.LOST -> {
            text = "Perdida"
            bgColor = Color(0xFFFFE5E5)
            fgColor = Color(0xFFC62828)
        }

        ReportType.FOUND -> {
            text = "Encontrada"
            bgColor = TextAccent.copy(alpha = 0.18f)
            fgColor = TextAccent
        }
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fgColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReportStatusChip(status: ReportStatus) {
    val text: String
    val bgColor: Color
    val fgColor: Color

    when (status) {
        ReportStatus.PENDING -> {
            text = "Pendiente"
            bgColor = Color(0xFFFFE9CC)
            fgColor = Color(0xFFB26A00)
        }

        ReportStatus.RESOLVED -> {
            text = "Resuelto"
            bgColor = TextAccent.copy(alpha = 0.18f)
            fgColor = TextAccent
        }
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fgColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}


