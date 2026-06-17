package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.model.ReportResponse
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.util.TranslationUtils
import com.example.sanosysalvosv2.viewmodel.UserReportsUiState
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel

@Composable
fun ReportesScreen(
    reportViewModel: UserReportsViewModel,
    onNavigateToNewReport: () -> Unit,
    onNavigateToReporteDetail: (String) -> Unit,
    onNavigateToEditReport: (String) -> Unit,
) {
    val uiState by reportViewModel.uiState.collectAsState()
    val activeFilter by reportViewModel.activeFilter.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        reportViewModel.loadMyReports()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UserReportsUiState.Deleted -> {
                snackbarHostState.showSnackbar("Reporte eliminado")
                reportViewModel.loadMyReports()
            }
            is UserReportsUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UserReportsUiState.Error).message)
            }
            else -> {}
        }
    }

    val reports = when (uiState) {
        is UserReportsUiState.Success -> (uiState as UserReportsUiState.Success).reports
        else -> emptyList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewReport,
                containerColor = Color(0xFF4A9B8E),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo reporte",
                    tint = Color.White,
                )
            }
        },
        containerColor = Color.White,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
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
                        selected = activeFilter == "LOST",
                        onClick = { reportViewModel.setFilter("LOST") },
                    )
                    FilterChip(
                        label = "Encontrados",
                        selected = activeFilter == "FOUND",
                        onClick = { reportViewModel.setFilter("FOUND") },
                    )
                    FilterChip(
                        label = "Todos",
                        selected = activeFilter == null,
                        onClick = { reportViewModel.setFilter(null) },
                    )
                }
            }

            when (uiState) {
                is UserReportsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TextAccent)
                    }
                }
                is UserReportsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (uiState as UserReportsUiState.Error).message,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                is UserReportsUiState.Success -> {
                    if (reports.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No hay reportes para mostrar.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(reports) { report ->
                                ReportCard(
                                    report = report,
                                    onClick = { onNavigateToReporteDetail(report.id) },
                                    onEdit = { onNavigateToEditReport(report.id) },
                                    onDelete = { showDeleteDialog = report.id },
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

            if (showDeleteDialog != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Eliminar reporte") },
                    text = {
                        Text("¿Estás seguro que deseas eliminar este reporte? Esta acción no se puede deshacer.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                reportViewModel.deleteReport(showDeleteDialog!!)
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        ) {
                            Text("Eliminar", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Cancelar")
                        }
                    },
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
    report: ReportResponse,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val reportType = report.type.uppercase()
    val reportStatus = report.status?.uppercase()

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
                ReportTypeBadge(type = reportType)
                ReportStatusChip(status = reportStatus)
            }

            Text(
                text = report.description.orEmpty().ifBlank { "Sin descripción" },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            report.locationName?.takeIf { it.isNotBlank() }?.let { location ->
                Text(
                    text = "📍 $location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            Text(
                text = "📅 ${report.eventDate ?: report.createdAt ?: "Fecha desconocida"}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (reportStatus == null || reportStatus == "PENDING") {
                    OutlinedButton(
                        onClick = onEdit,
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A9B8E)),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4A9B8E))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                OutlinedButton(
                    onClick = onDelete,
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun ReportTypeBadge(type: String) {
    val displayType = TranslationUtils.reportType(type)
    val bgColor = if (type.uppercase() == "FOUND") TextAccent.copy(alpha = 0.18f) else Color(0xFFFFE5E5)
    val fgColor = if (type.uppercase() == "FOUND") TextAccent else Color(0xFFC62828)

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = displayType,
            style = MaterialTheme.typography.labelMedium,
            color = fgColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReportStatusChip(status: String?) {
    val text = TranslationUtils.reportStatus(status)
    val fgColor = TranslationUtils.statusColor(status)
    val bgColor = fgColor.copy(alpha = 0.12f)

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
