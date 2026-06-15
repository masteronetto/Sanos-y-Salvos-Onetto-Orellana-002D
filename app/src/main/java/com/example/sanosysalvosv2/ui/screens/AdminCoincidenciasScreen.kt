package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.model.AdminCoincidenciaSummary
import com.example.sanosysalvosv2.viewmodel.AdminMatchesViewModel
import com.example.sanosysalvosv2.viewmodel.AdminMatchesUiState

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

@Composable
fun AdminCoincidenciasScreen(
    onLogout: () -> Unit,
    onNavigateToMatchDetail: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel = remember { AdminMatchesViewModel(context.applicationContext as Application) }
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedToggle by remember { mutableStateOf(MatchToggle.ALL) }
    var comunaExpanded by remember { mutableStateOf(false) }
    var selectedComuna by remember { mutableStateOf(MatchComunaFilter.ALL) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    val matches: List<AdminCoincidenciaSummary> = when (uiState) {
        is AdminMatchesUiState.Success -> (uiState as AdminMatchesUiState.Success).matches
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllMatches()
    }

    val filteredMatches = matches.filter { match ->
        val matchesToggle = when (selectedToggle) {
            MatchToggle.PENDING -> match.status.equals("PENDING", ignoreCase = true)
            MatchToggle.CONFIRMED -> match.status.equals("CONFIRMED", ignoreCase = true)
            MatchToggle.REJECTED -> match.status.equals("DISCARDED", ignoreCase = true) || match.status.equals("REJECTED", ignoreCase = true)
            MatchToggle.ALL -> true
        }
        val matchesSearch = searchQuery.isBlank() || listOf(match.id, match.sourceName, match.matchedName, match.comuna, match.date)
            .any { value -> value.contains(searchQuery, ignoreCase = true) }
        val matchesComuna = selectedComuna == MatchComunaFilter.ALL || match.comuna.equals(selectedComuna.label, ignoreCase = true)
        matchesToggle && matchesSearch && matchesComuna
    }

    LaunchedEffect(filteredMatches.size) {
        if (selectedRowIndex >= filteredMatches.size) {
            selectedRowIndex = -1
        }
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
                ToggleButton("Pendientes", selectedToggle == MatchToggle.PENDING) {
                    selectedToggle = MatchToggle.PENDING
                }
                ToggleButton("Confirmados", selectedToggle == MatchToggle.CONFIRMED) {
                    selectedToggle = MatchToggle.CONFIRMED
                }
                ToggleButton("Descartadas", selectedToggle == MatchToggle.REJECTED) {
                    selectedToggle = MatchToggle.REJECTED
                }
                ToggleButton("Todas", selectedToggle == MatchToggle.ALL) {
                    selectedToggle = MatchToggle.ALL
                }
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
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleActionButton(
                    icon = Icons.Filled.Delete,
                    onClick = {
                        if (selectedRowIndex in filteredMatches.indices) {
                            showDiscardConfirmation = true
                        }
                    },
                )
            }

            when (uiState) {
                is AdminMatchesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AdminMatchesUiState.Error -> {
                    Text(
                        text = (uiState as AdminMatchesUiState.Error).message,
                        color = RejectedRed,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                is AdminMatchesUiState.Success -> {
                    if (filteredMatches.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron coincidencias.")
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                        ) {
                            Column(modifier = Modifier.width(700.dp)) {
                                TableHeader()

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    itemsIndexed(filteredMatches) { index, match ->
                                        MatchRow(
                                            match = match,
                                            selected = selectedRowIndex == index,
                                            onClick = {
                                                selectedRowIndex = index
                                                onNavigateToMatchDetail(match.id)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDiscardConfirmation) {
                val selectedMatch = filteredMatches.getOrNull(selectedRowIndex)
                AlertDialog(
                    onDismissRequest = { showDiscardConfirmation = false },
                    title = { Text("Descartar coincidencia") },
                    text = { Text("¿Estás seguro de que quieres descartar esta coincidencia?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedMatch?.let { viewModel.discardMatch(it.id) }
                                showDiscardConfirmation = false
                            },
                        ) {
                            Text("Descartar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDiscardConfirmation = false }) {
                            Text("Cancelar")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .width(700.dp)
            .background(Color.White)
            .border(1.dp, BorderColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell("ID", width = 48.dp)
        HeaderCell("Nombre", width = 140.dp)
        HeaderCell("Encontrado", width = 220.dp)
        HeaderCell("Coincidencia %", width = 110.dp)
        HeaderCell("Estado", width = 90.dp)
        HeaderCell("Fecha", width = 90.dp)
    }
}

@Composable
private fun MatchRow(
    match: AdminCoincidenciaSummary,
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
            modifier = Modifier.width(700.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(match.id, width = 48.dp)
            TableCell(match.sourceName, width = 140.dp, bold = true)
            TableCell(match.matchedName, width = 220.dp)
            TableCell("${match.score}%", width = 110.dp, bold = true)
            TableCell(match.status, width = 90.dp, color = matchStatusColorFromString(match.status), bold = true, overflow = TextOverflow.Clip)
            TableCell(match.date.ifEmpty { "—" }, width = 90.dp)
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
    width: Dp,
) {
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
    width: Dp,
    color: Color = Color.Black,
    bold: Boolean = false,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        color = color,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = overflow,
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

private fun matchStatusColorFromString(status: String): Color = when (status.uppercase()) {
    "PENDING" -> PendingOrange
    "CONFIRMED" -> ConfirmedTeal
    "DISCARDED", "REJECTED" -> RejectedRed
    else -> Color.Black
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