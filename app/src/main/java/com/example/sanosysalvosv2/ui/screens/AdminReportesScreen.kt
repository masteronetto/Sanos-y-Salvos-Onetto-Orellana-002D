package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.compose.runtime.getValue
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel
import com.example.sanosysalvosv2.model.AdminReportSummary
import com.example.sanosysalvosv2.util.TranslationUtils
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.theme.TextSecondary

private val Teal = Color(0xFF0F8A8A)
private val TealSoft = Color(0xFFEAF7F6)
private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val BorderColor = Color(0xFFD7E5E3)
private val PendingOrange = Color(0xFFE08A18)
private val ResolvedTeal = Color(0xFF0F8A8A)
private val RejectedRed = Color(0xFFC53B3B)

private enum class ReportCaseType(val label: String) {
    LOST("Perdidos"),
    FOUND("Encontrados"),
    ALL("Todos"),
}

private enum class ReportFilterStatus(val label: String) {
    ALL("Todos"),
    PENDING("Pendiente"),
    APPROVED("Aprobado"),
    REJECTED("Rechazado"),
}

private enum class ReportComunaFilter(val label: String) {
    ALL("Todas"),
    MAIPU("Maipú"),
    PROVIDENCIA("Providencia"),
    NUNOA("Ñuñoa"),
    SANTIAGO("Santiago"),
}

// Using real report model from backend

@Composable
fun AdminReportesScreen(
    onNavigateToReporteDetail: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = remember { UserReportsViewModel(context.applicationContext as Application) }
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var caseFilter by remember { mutableStateOf(ReportCaseType.ALL) }
    var comunaExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedComuna by remember { mutableStateOf(ReportComunaFilter.ALL) }
    var selectedStatus by remember { mutableStateOf(ReportFilterStatus.ALL) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(caseFilter, selectedStatus, selectedComuna) {
        val type = if (caseFilter == ReportCaseType.ALL) null else caseFilter.name
        viewModel.loadAllReports(type = type)
    }

    // Map UI state to list for display
    val reports: List<AdminReportSummary> = when (uiState) {
        is com.example.sanosysalvosv2.viewmodel.UserReportsUiState.Success -> (uiState as com.example.sanosysalvosv2.viewmodel.UserReportsUiState.Success).reports.map { r ->
            AdminReportSummary(
                id = r.id,
                name = if (r.species != null) TranslationUtils.species(r.species) else (r.breed ?: r.locationName ?: "-"),
                reportedBy = r.reporterId ?: "-",
                comuna = r.locationName ?: "-",
                date = r.eventDate ?: r.createdAt ?: "-",
                caseType = r.type ?: "",
                status = r.status ?: "",
            )
        }
        else -> emptyList()
    }

    val filteredReports = reports.filter { report ->
        val matchesSearch = searchQuery.isBlank() || listOf(report.id, report.name, report.reportedBy, report.comuna, report.date)
            .any { value -> value.contains(searchQuery, ignoreCase = true) }
        val matchesCase = caseFilter == ReportCaseType.ALL || report.caseType.equals(caseFilter.name, ignoreCase = true)
        val matchesComuna = selectedComuna == ReportComunaFilter.ALL || report.comuna.equals(selectedComuna.label, ignoreCase = true)
        val matchesStatus = selectedStatus == ReportFilterStatus.ALL || report.status.equals(selectedStatus.name, ignoreCase = true)
        matchesSearch && matchesCase && matchesComuna && matchesStatus
    }

    androidx.compose.material3.Scaffold(
        topBar = { AdminTopBar(title = "Reportes", onLogout = onLogout) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Gestión de reportes",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                )
                Text(
                    text = "Visualiza las mascotas y su caso",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayText,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleButton(text = "Perdidos", selected = caseFilter == ReportCaseType.LOST, onClick = { caseFilter = ReportCaseType.LOST })
                    ToggleButton(text = "Encontrados", selected = caseFilter == ReportCaseType.FOUND, onClick = { caseFilter = ReportCaseType.FOUND })
                    ToggleButton(text = "Todos", selected = caseFilter == ReportCaseType.ALL, onClick = { caseFilter = ReportCaseType.ALL })
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar reporte...") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DropdownChip(
                        label = "Comunas",
                        value = selectedComuna.label,
                        expanded = comunaExpanded,
                        onExpandedChange = { comunaExpanded = it },
                        onDismiss = { comunaExpanded = false },
                        options = ReportComunaFilter.entries,
                        optionLabel = { it.label },
                        onOptionSelected = { selectedComuna = it },
                    )

                    DropdownChip(
                        label = "Estado",
                        value = selectedStatus.label,
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it },
                        onDismiss = { statusExpanded = false },
                        options = ReportFilterStatus.entries,
                        optionLabel = { it.label },
                        onOptionSelected = { selectedStatus = it },
                    )
                }

                TableHeader()
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredReports) { report ->
                    ReportRow(
                        report = report,
                        selected = false,
                        onClick = { onNavigateToReporteDetail(report.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, BorderColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell("ID", 60.dp)
        HeaderCell("Nombre", 140.dp)
        HeaderCell("Reportado por", 160.dp)
        HeaderCell("Comuna", 100.dp)
        HeaderCell("Fecha", 100.dp)
        HeaderCell("Estado", 90.dp)
    }
}

@Composable
private fun ReportRow(
    report: AdminReportSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val rowBackground = if (selected) TealSoft else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(report.id, 60.dp)
            TableCell(report.name, 140.dp, bold = true)
            TableCell(report.reportedBy, 160.dp)
            TableCell(report.comuna, 100.dp)
            TableCell(report.date, 100.dp)
            TableCell(
                text = TranslationUtils.reportStatus(report.status),
                width = 90.dp,
                color = TranslationUtils.statusColor(report.status),
                bold = true
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderColor),
        )
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        color = GrayText,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = Color.Black,
    bold: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        color = color,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) Teal else Color.White
    val contentColor = if (selected) Color.White else Teal

    Box(
        modifier = Modifier
            .border(1.dp, Teal, RoundedCornerShape(999.dp))
            .background(background, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text = text, color = contentColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun <T> DropdownChip(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
) where T : Enum<T> {
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .border(1.dp, BorderColor, RoundedCornerShape(999.dp))
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "$label: $value", color = Color.Black, fontWeight = FontWeight.Medium)
            Text(text = "▾", color = GrayText, fontWeight = FontWeight.Bold)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        onDismiss()
                    },
                )
            }
        }
    }
}

private fun reportStatusColor(status: ReportFilterStatus): Color = when (status) {
    ReportFilterStatus.PENDING -> PendingOrange
    ReportFilterStatus.APPROVED -> ResolvedTeal
    ReportFilterStatus.REJECTED -> RejectedRed
    ReportFilterStatus.ALL -> Color.Black
}

@Composable
private fun AdminTopBar(title: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        androidx.compose.material3.TextButton(onClick = onLogout) {
            Text(text = "Salir")
        }
    }
}