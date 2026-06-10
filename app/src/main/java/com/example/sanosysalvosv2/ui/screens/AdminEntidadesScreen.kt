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
private val ActiveColor = Color(0xFF0F8A8A)
private val PendingOrange = Color(0xFFE08A18)

private enum class EntityType(val label: String) {
    ALL("Todos"),
    CLINICA("Clínica Veterinaria"),
    REFUGIO("Refugio"),
    MUNICIPALIDAD("Municipalidad"),
}

private enum class EntityStatus(val label: String) {
    ALL("Todos"),
    ACTIVE("Activo"),
    PENDING("Pendiente"),
}

private data class AdminEntityRowMock(
    val id: String,
    val name: String,
    val type: String,
    val comuna: String,
    val status: EntityStatus,
)

@Composable
fun AdminEntidadesScreen(
    onLogout: () -> Unit,
) {
    val entities = remember {
        listOf(
            AdminEntityRowMock("E001", "Clinica Vitpatas", "Clinica Veterinaria", "Cerrillos", EntityStatus.ACTIVE),
            AdminEntityRowMock("E002", "Refugio Huellas", "info@refugiohuellas.cl", "Pucón", EntityStatus.PENDING),
            AdminEntityRowMock("E003", "Municipalidad Ñuñoa", "contacto@nunua.cl", "Ñuñoa", EntityStatus.ACTIVE),
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(EntityType.ALL) }
    var selectedStatus by remember { mutableStateOf(EntityStatus.ALL) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }

    val filteredEntities = entities.filter { entity ->
        val matchesSearch = searchQuery.isBlank() || listOf(entity.id, entity.name, entity.type, entity.comuna)
            .any { value -> value.contains(searchQuery, ignoreCase = true) }
        val matchesType = selectedType == EntityType.ALL || entity.type.contains(selectedType.label, ignoreCase = true)
        val matchesStatus = selectedStatus == EntityStatus.ALL || entity.status == selectedStatus
        matchesSearch && matchesType && matchesStatus
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
                    onClick = { /* placeholder */ },
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
                    label = "Especie",
                    value = selectedType.label,
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    onDismiss = { typeExpanded = false },
                    options = EntityType.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { selectedType = it },
                )
                DropdownChip(
                    label = "Estado",
                    value = selectedStatus.label,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onDismiss = { statusExpanded = false },
                    options = EntityStatus.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { selectedStatus = it },
                )

                Spacer(modifier = Modifier.weight(1f))

                CircleActionButton(icon = Icons.Filled.Delete, onClick = { /* placeholder */ })
                CircleActionButton(icon = Icons.Filled.Edit, onClick = { /* placeholder */ })
            }

            TableHeader()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                itemsIndexed(filteredEntities) { index, entity ->
                    EntityRow(
                        entity = entity,
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
        HeaderCell("Nombre", 1.5f)
        HeaderCell("Tipo", 1.6f)
        HeaderCell("Comuna", 1.0f)
        HeaderCell("Estado", 1.0f)
    }
}

@Composable
private fun EntityRow(
    entity: AdminEntityRowMock,
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
            TableCell(text = entity.id, weight = 0.8f)
            TableCell(text = entity.name, weight = 1.5f, bold = true)
            TableCell(text = entity.type, weight = 1.6f)
            TableCell(text = entity.comuna, weight = 1.0f)
            TableCell(
                text = entity.status.label,
                weight = 1.0f,
                color = entityStatusColor(entity.status),
                bold = true,
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