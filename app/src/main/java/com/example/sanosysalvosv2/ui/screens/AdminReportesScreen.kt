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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
    RESOLVED("Resuelto"),
    REJECTED("Rechazado"),
}

private enum class ReportComunaFilter(val label: String) {
    ALL("Todas"),
    MAIPU("Maipú"),
    PROVIDENCIA("Providencia"),
    NUNOA("Ñuñoa"),
    SANTIAGO("Santiago"),
}

private data class AdminReportRowMock(
    val id: String,
    val name: String,
    val reportedBy: String,
    val comuna: String,
    val date: String,
    val caseType: ReportCaseType,
    val status: ReportFilterStatus,
)

@Composable
fun AdminReportesScreen(
    onNavigateToReporteDetail: (String) -> Unit,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        // The actual admin data path stays wired through the shared architecture.
    }

    val reports = remember {
        listOf(
            AdminReportRowMock("R001", "Perla", "Camila Orellana", "Maipú", "02/06/2026", ReportCaseType.LOST, ReportFilterStatus.PENDING),
            AdminReportRowMock("R002", "Masu", "Valentina Perez", "Providencia", "03/06/2026", ReportCaseType.FOUND, ReportFilterStatus.RESOLVED),
            AdminReportRowMock("R003", "Cachupin", "Carlos Gómez", "Ñuñoa", "04/06/2026", ReportCaseType.LOST, ReportFilterStatus.PENDING),
            AdminReportRowMock("R004", "Mishi", "José Muñoz", "Santiago", "05/06/2026", ReportCaseType.FOUND, ReportFilterStatus.REJECTED),
            AdminReportRowMock("R005", "Coco", "Municipalidad Ñuñoa", "Ñuñoa", "06/06/2026", ReportCaseType.LOST, ReportFilterStatus.RESOLVED),
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var caseFilter by remember { mutableStateOf(ReportCaseType.ALL) }
    var comunaExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedComuna by remember { mutableStateOf(ReportComunaFilter.ALL) }
    var selectedStatus by remember { mutableStateOf(ReportFilterStatus.ALL) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }

    val filteredReports = reports.filter { report ->
        val matchesSearch = searchQuery.isBlank() || listOf(report.id, report.name, report.reportedBy, report.comuna, report.date)
            .any { value -> value.contains(searchQuery, ignoreCase = true) }
        val matchesCase = caseFilter == ReportCaseType.ALL || report.caseType == caseFilter
        val matchesComuna = selectedComuna == ReportComunaFilter.ALL || report.comuna.equals(selectedComuna.label, ignoreCase = true)
        val matchesStatus = selectedStatus == ReportFilterStatus.ALL || report.status == selectedStatus
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar reporte...") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

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

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleActionButton(icon = Icons.Filled.Delete, onClick = { /* placeholder */ })
                CircleActionButton(icon = Icons.Filled.Edit, onClick = { /* placeholder */ })
            }

            TableHeader()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(filteredReports) { index, report ->
                    ReportRow(
                        report = report,
                        selected = selectedRowIndex == index,
                        onClick = {
                            selectedRowIndex = index
                            onNavigateToReporteDetail(report.id)
                        },
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
        HeaderCell("ID", 0.8f)
        HeaderCell("Nombre", 1.3f)
        HeaderCell("Reportado por", 1.6f)
        HeaderCell("Comuna", 1.2f)
        HeaderCell("Fecha", 1.1f)
        HeaderCell("Estado", 0.9f)
    }
}

@Composable
private fun ReportRow(
    report: AdminReportRowMock,
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
            TableCell(report.id, 0.8f)
            TableCell(report.name, 1.3f, bold = true)
            TableCell(report.reportedBy, 1.6f)
            TableCell(report.comuna, 1.2f)
            TableCell(report.date, 1.1f)
            TableCell(report.status.label, 0.9f, color = reportStatusColor(report.status), bold = true)
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
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        color = GrayText,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color = Color.Black,
    bold: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
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

@Composable
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .background(Teal, RoundedCornerShape(999.dp)),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

private fun reportStatusColor(status: ReportFilterStatus): Color = when (status) {
    ReportFilterStatus.PENDING -> PendingOrange
    ReportFilterStatus.RESOLVED -> ResolvedTeal
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