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
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.viewmodel.AdminMascotasViewModel
import com.example.sanosysalvosv2.viewmodel.AdminMascotasUiState
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
import com.example.sanosysalvosv2.viewmodel.AdminViewModel

private val Teal = Color(0xFF0F8A8A)
private val TealDark = Color(0xFF0F5B5B)
private val TealSoft = Color(0xFFEAF7F6)
private val BorderColor = Color(0xFFD7E5E3)
private val TitleGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val ActiveColor = Teal
private val InactiveColor = Color(0xFFC53B3B)

private data class AdminPetRowMock(
    val id: String,
    val name: String,
    val type: PetType,
    val breed: String,
    val owner: String,
    val status: PetStatus,
)

private enum class PetType(val label: String) {
    TODOS("Todos"),
    PERRO("Perro"),
    GATO("Gato"),
}

private enum class PetStatus(val label: String) {
    TODOS("Todos"),
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
}

@Composable
fun AdminMascotasScreen(
    onLogout: () -> Unit,
    onNavigateToEditPet: (String?) -> Unit,
) {
    val viewModel: AdminMascotasViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.loadAllPets() }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var speciesExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedSpecies by remember { mutableStateOf(PetType.TODOS) }
    var selectedStatus by remember { mutableStateOf(PetStatus.TODOS) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }
    var selectedPetId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val pets = when (uiState) {
        is AdminMascotasUiState.Success -> (uiState as AdminMascotasUiState.Success).pets
        else -> emptyList()
    }

    val filteredPets = pets.filter { pet ->
        val matchesSearch = searchQuery.isBlank() || listOf(pet.id, pet.name, pet.species, pet.breed)
            .any { value -> value.contains(searchQuery, ignoreCase = true) }
        val matchesSpecies = selectedSpecies == PetType.TODOS || pet.species.equals(selectedSpecies.label, ignoreCase = true)
        val matchesStatus = selectedStatus == PetStatus.TODOS || (pet.age > 0 && selectedStatus == PetStatus.ACTIVO) || (pet.age == 0 && selectedStatus == PetStatus.INACTIVO)
        matchesSearch && matchesSpecies && matchesStatus
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
                text = "Mascotas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TitleGreen,
            )
            Text(
                text = "Administra a las mascotas registradas",
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
                    placeholder = { Text("Buscar mascota...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                Button(
                    onClick = { onNavigateToEditPet(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = "+ Nueva mascota", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DropdownChip(
                    label = "Especie",
                    value = selectedSpecies.label,
                    expanded = speciesExpanded,
                    onExpandedChange = { speciesExpanded = it },
                    onDismiss = { speciesExpanded = false },
                    options = PetType.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { selectedSpecies = it },
                )
                DropdownChip(
                    label = "Estado",
                    value = selectedStatus.label,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onDismiss = { statusExpanded = false },
                    options = PetStatus.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { selectedStatus = it },
                )

                Spacer(modifier = Modifier.weight(1f))

                CircleActionButton(icon = Icons.Filled.Delete, onClick = { showDeleteDialog = true },)
                CircleActionButton(icon = Icons.Filled.Edit, onClick = { selectedPetId?.let { onNavigateToEditPet(it) } },)
            }

            if (uiState is AdminMascotasUiState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator() }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
            ) {
                Column(modifier = Modifier.width(620.dp)) {
                    TableHeader()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        itemsIndexed(filteredPets) { index, pet ->
                            PetTableRow(
                                pet = AdminPetRowMock(pet.id, pet.name, if (pet.species.equals("Perro", true)) PetType.PERRO else PetType.GATO, pet.breed, pet.ownerId, if (pet.age > 0) PetStatus.ACTIVO else PetStatus.INACTIVO),
                                selected = selectedRowIndex == index,
                                onClick = {
                                    selectedRowIndex = index
                                    selectedPetId = pet.id
                                },
                            )
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(text = "Eliminar mascota") },
                    text = { Text(text = "¿Seguro que deseas eliminar esta mascota?") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            selectedPetId?.let { viewModel.deletePet(it) }
                            showDeleteDialog = false
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
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
            .width(620.dp)
            .background(Color.White)
            .border(1.dp, BorderColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell(text = "ID", width = 48.dp)
        HeaderCell(text = "Nombre", width = 140.dp)
        HeaderCell(text = "Tipo", width = 100.dp)
        HeaderCell(text = "Raza", width = 140.dp)
        HeaderCell(text = "Dueño", width = 160.dp)
        HeaderCell(text = "Estado", width = 80.dp)
    }
}

@Composable
private fun PetTableRow(
    pet: AdminPetRowMock,
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
            modifier = Modifier.width(620.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(text = pet.id, width = 48.dp)
            TableCell(text = pet.name, width = 140.dp, bold = true)
            TableCell(text = pet.type.label, width = 100.dp)
            TableCell(text = pet.breed, width = 140.dp)
            TableCell(text = pet.owner, width = 160.dp)
            TableCell(
                text = pet.status.label,
                width = 80.dp,
                color = petStatusColor(pet.status),
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
private fun RowScope.HeaderCell(
    text: String,
    width: Dp,
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontWeight = FontWeight.Bold,
        color = GrayText,
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
            Text(text = "$label: $value", color = Color.Black, fontWeight = FontWeight.Medium)
            Text(text = "▾", color = GrayText, fontWeight = FontWeight.Bold)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
        ) {
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

private fun petStatusColor(status: PetStatus): Color = when (status) {
    PetStatus.ACTIVO -> ActiveColor
    PetStatus.INACTIVO -> InactiveColor
    PetStatus.TODOS -> Color.Black
}

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        androidx.compose.material3.TextButton(onClick = onLogout) {
            Text(text = "Salir")
        }
    }
}

@Composable
private fun adminViewModelHasError(): Boolean = false