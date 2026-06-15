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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.model.CollaboratorRequest
import com.example.sanosysalvosv2.model.CollaboratorResponse
import com.example.sanosysalvosv2.viewmodel.AdminEntidadesUiState
import com.example.sanosysalvosv2.viewmodel.AdminEntidadesViewModel

private val Teal = Color(0xFF0F8A8A)
private val TealSoft = Color(0xFFEAF7F6)
private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val BorderColor = Color(0xFFD7E5E3)
private val ActiveColor = Color(0xFF0F8A8A)
private val PendingOrange = Color(0xFFE08A18)

private enum class EntityType(val label: String, val value: String) {
    ALL("Todos", ""),
    VETERINARY_CLINIC("Clínica", "VETERINARY_CLINIC"),
    REFUGIO("Refugio", "SHELTER"),
    MUNICIPALIDAD("Municipalidad", "MUNICIPALITY"),
    VOLUNTARIO("Voluntario", "VOLUNTEER"),
}

private enum class EntityStatus(val label: String, val value: String) {
    ALL("Todos", ""),
    ACTIVE("Activo", "ACTIVE"),
    PENDING("Pendiente", "PENDING"),
    INACTIVE("Inactivo", "INACTIVE"),
    SUSPENDED("Suspendido", "SUSPENDED"),
    OTHER("Otro", "OTHER");

    companion object {
        fun fromValue(value: String): EntityStatus {
            return values().firstOrNull { it.value.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

@Composable
fun AdminEntidadesScreen(
    onLogout: () -> Unit,
    onNavigateToEditEntidad: (String?) -> Unit,
) {
    val viewModel: AdminEntidadesViewModel = viewModel()
    LaunchedEffect(Unit) {
        viewModel.loadEntidades()
    }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(EntityType.ALL) }
    var selectedStatus by remember { mutableStateOf(EntityStatus.ALL) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }
    var selectedEntidadId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val collaborators = when (uiState) {
        is AdminEntidadesUiState.Success -> (uiState as AdminEntidadesUiState.Success).collaborators
        else -> emptyList()
    }

    val filteredCollaborators = collaborators.filter { collaborator ->
        val matchesSearch = searchQuery.isBlank() || listOf(
            collaborator.name,
            collaborator.email,
            collaborator.phone,
            collaborator.comuna,
            collaborator.address.orEmpty(),
        ).any { value -> value.contains(searchQuery, ignoreCase = true) }

        val matchesStatus = selectedStatus == EntityStatus.ALL || collaborator.status.equals(selectedStatus.value, ignoreCase = true)
        matchesSearch && matchesStatus
    }

    fun clearSelection() {
        selectedRowIndex = -1
        selectedEntidadId = null
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
                text = "Entidades colaboradoras",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
            )
            Text(
                text = "Administra a las entidades registradas",
                style = MaterialTheme.typography.bodyLarge,
                color = GrayText,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar entidad...") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                Button(
                    onClick = { onNavigateToEditEntidad(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = "+ Nueva entidad", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DropdownChip(
                    label = "Tipo",
                    value = selectedType.label,
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    onDismiss = { typeExpanded = false },
                    options = EntityType.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = {
                        selectedType = it
                        clearSelection()
                        if (it == EntityType.ALL) viewModel.loadEntidades() else viewModel.loadByType(it.value)
                    },
                )
                DropdownChip(
                    label = "Estado",
                    value = selectedStatus.label,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onDismiss = { statusExpanded = false },
                    options = EntityStatus.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { selectedStatus = it },
                )

                Spacer(modifier = Modifier.weight(1f))

                CircleActionButton(
                    icon = Icons.Filled.Delete,
                    onClick = { showDeleteDialog = true },
                )
                CircleActionButton(
                    icon = Icons.Filled.Edit,
                    onClick = { selectedEntidadId?.let { onNavigateToEditEntidad(it) } },
                )
            }

            when (uiState) {
                is AdminEntidadesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
                is AdminEntidadesUiState.Error -> {
                    Text(
                        text = (uiState as AdminEntidadesUiState.Error).message,
                        color = Color(0xFFC53B3B),
                    )
                }
                is AdminEntidadesUiState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        Column(modifier = Modifier.width(518.dp)) {
                            TableHeader()

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                            ) {
                                itemsIndexed(filteredCollaborators) { index, collaborator ->
                                    EntityRow(
                                        entity = collaborator,
                                        selected = selectedRowIndex == index,
                                        onClick = {
                                            selectedRowIndex = index
                                            selectedEntidadId = collaborator.id
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && selectedEntidadId != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Eliminar entidad") },
                text = { Text(text = "¿Deseas eliminar esta entidad de forma permanente?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteEntidad(selectedEntidadId!!)
                        showDeleteDialog = false
                        clearSelection()
                    }) {
                        Text(text = "Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = "Cancelar")
                    }
                },
            )
        }
    }
}

private fun entityStatusColor(status: String): Color = when (EntityStatus.fromValue(status)) {
    EntityStatus.ACTIVE -> ActiveColor
    EntityStatus.PENDING -> PendingOrange
    EntityStatus.INACTIVE -> GrayText
    EntityStatus.SUSPENDED -> Color.Red
    EntityStatus.OTHER -> Color.Black
    EntityStatus.ALL -> Color.Black
}

@Composable
private fun EntityRow(
    entity: CollaboratorResponse,
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
            modifier = Modifier.width(518.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(text = entity.id, width = 48.dp)
            TableCell(text = entity.name, width = 140.dp, bold = true)
            TableCell(text = entity.type, width = 120.dp)
            TableCell(text = entity.comuna, width = 120.dp)
            TableCell(
                text = EntityStatus.fromValue(entity.status).label,
                width = 70.dp,
                color = entityStatusColor(entity.status),
                bold = true,
                overflow = TextOverflow.Clip,
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
private fun TableHeader() {
    Row(
        modifier = Modifier
            .width(498.dp)
            .background(Color.White)
            .border(1.dp, BorderColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell("ID", width = 48.dp)
        HeaderCell("Nombre", width = 140.dp)
        HeaderCell("Tipo", width = 120.dp)
        HeaderCell("Comuna", width = 120.dp)
        HeaderCell("Estado", width = 70.dp)
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
            Text(text = "$label $value", color = Color.Black, fontWeight = FontWeight.Medium)
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

private fun entityStatusColor(status: EntityStatus): Color = when (status) {
    EntityStatus.ACTIVE -> ActiveColor
    EntityStatus.PENDING -> PendingOrange
    EntityStatus.INACTIVE -> GrayText
    EntityStatus.SUSPENDED -> Color.Red
    EntityStatus.OTHER -> Color.Black
    EntityStatus.ALL -> Color.Black
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