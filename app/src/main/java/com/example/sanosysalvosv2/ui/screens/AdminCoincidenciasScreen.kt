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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val Teal = Color(0xFF0F8A8A)
private val TealSoft = Color(0xFFEAF7F6)
private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val BorderColor = Color(0xFFD7E5E3)
private val PendingOrange = Color(0xFFE08A18)
private val ConfirmedTeal = Color(0xFF0F8A8A)
private val RejectedRed = Color(0xFFC53B3B)

private enum class MatchToggle(val label: String) {
    PENDING("Pendientes"),
    CONFIRMED("Confirmados"),
    REJECTED("Descartadas"),
    ALL("Todas"),
}

private enum class MatchComunaFilter(val label: String) {
    ALL("Todas"),
    MAIPU("Maipú"),
    PROVIDENCIA("Providencia"),
    NUNOA("Ñuñoa"),
    SANTIAGO("Santiago"),
}

private enum class MatchStatus(val label: String) {
    ALL("Todos"),
    PENDING("Pendiente"),
    CONFIRMED("Confirmado"),
    REJECTED("Descartado"),
}

private data class AdminMatchRowMock(
    val id: String,
    val name: String,
    val found: String,
    val percent: Int,
    val status: MatchStatus,
    val date: String?,
    val comuna: String,
)

@Composable
fun AdminCoincidenciasScreen(
    onLogout: () -> Unit,
) {
    val matches = remember {
        listOf(
            AdminMatchRowMock("C001", "Kisti", "Perro sin nombre", 92, MatchStatus.PENDING, "18/09/2025", "Maipú"),
            AdminMatchRowMock("C002", "Perla", "Perro sin nombre", 87, MatchStatus.PENDING, "29/12/2025", "Providencia"),
            AdminMatchRowMock("C003", "Cachupin", "Perro sin nombre", 76, MatchStatus.REJECTED, "18/09/2025", "Ñuñoa"),
            AdminMatchRowMock("C004", "Mishi", "Perro sin nombre", 89, MatchStatus.CONFIRMED, "29/12/2025", "Santiago"),
            AdminMatchRowMock("C005", "Toby", "Perro sin nombre", 83, MatchStatus.PENDING, null, "Maipú"),
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedToggle by remember { mutableStateOf(MatchToggle.ALL) }
    var comunaExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedComuna by remember { mutableStateOf(MatchComunaFilter.ALL) }
    var selectedStatus by remember { mutableStateOf(MatchStatus.ALL) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }

    val filteredMatches = matches.filter { match ->
        val matchesToggle = when (selectedToggle) {
            MatchToggle.PENDING -> match.status == MatchStatus.PENDING
            MatchToggle.CONFIRMED -> match.status == MatchStatus.CONFIRMED
            MatchToggle.REJECTED -> match.status == MatchStatus.REJECTED
            MatchToggle.ALL -> true
        }
        val matchesSearch = searchQuery.isBlank() || listOf(match.id, match.name, match.found, match.comuna, match.date.orEmpty())
            .any { value -> value.contains(searchQuery, ignoreCase = true) }
        val matchesComuna = selectedComuna == MatchComunaFilter.ALL || match.comuna.equals(selectedComuna.label, ignoreCase = true)
        val matchesStatus = selectedStatus == MatchStatus.ALL || match.status == selectedStatus
        matchesToggle && matchesSearch && matchesComuna && matchesStatus
    }

    androidx.compose.material3.Scaffold(
        topBar = { AdminTopBar(onLogout = onLogout) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Coincidencias detectadas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
            )
            Text(
                text = "Mira la comparación entre las mascotas y su coincidencia",
                style = MaterialTheme.typography.bodyLarge,
                color = GrayText,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleButton("Pendientes", selectedToggle == MatchToggle.PENDING) { selectedToggle = MatchToggle.PENDING }
                ToggleButton("Confirmados", selectedToggle == MatchToggle.CONFIRMED) { selectedToggle = MatchToggle.CONFIRMED }
                ToggleButton("Descartadas", selectedToggle == MatchToggle.REJECTED) { selectedToggle = MatchToggle.REJECTED }
                ToggleButton("Todas", selectedToggle == MatchToggle.ALL) { selectedToggle = MatchToggle.ALL }
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
                    placeholder = { Text("Buscar coincidencia...") },
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
                    options = MatchComunaFilter.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { selectedComuna = it },
                )

                DropdownChip(
                    label = "Estado",
                    value = selectedStatus.label,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onDismiss = { statusExpanded = false },
                    options = MatchStatus.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { selectedStatus = it },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleActionButton(icon = Icons.Filled.Delete, onClick = { /* placeholder */ })
                CircleActionButton(icon = Icons.Filled.Edit, onClick = { /* placeholder */ })
            }

            TableHeader()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                itemsIndexed(filteredMatches) { index, match ->
                    MatchRow(
                        match = match,
                        selected = selectedRowIndex == index,
                        onClick = { selectedRowIndex = index },
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
        HeaderCell("Nombre", 1.2f)
        HeaderCell("Encontrado", 1.8f)
        HeaderCell("Coincidencia %", 1.0f)
        HeaderCell("Estado", 1.0f)
        HeaderCell("Fecha", 0.9f)
    }
}

@Composable
private fun MatchRow(
    match: AdminMatchRowMock,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) TealSoft else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(match.id, 0.8f)
            TableCell(match.name, 1.2f, bold = true)
            TableCell(match.found, 1.8f)
            TableCell("${match.percent}%", 1.0f, bold = true)
            TableCell(match.status.label, 1.0f, color = matchStatusColor(match.status), bold = true)
            TableCell(match.date ?: "—", 0.9f)
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
private fun RowScope.HeaderCell(
    text: String,
    weight: Float,
) {
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

private fun matchStatusColor(status: MatchStatus): Color = when (status) {
    MatchStatus.PENDING -> PendingOrange
    MatchStatus.CONFIRMED -> ConfirmedTeal
    MatchStatus.REJECTED -> RejectedRed
    MatchStatus.ALL -> Color.Black
}

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        TextButton(onClick = onLogout) {
            Text(text = "Salir")
        }
    }
}