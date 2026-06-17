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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.model.PetResponse
import com.example.sanosysalvosv2.viewmodel.AdminMascotasUiState
import com.example.sanosysalvosv2.viewmodel.AdminMascotasViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Teal = Color(0xFF0F8A8A)
private val TealDark = Color(0xFF0F5B5B)
private val TealSoft = Color(0xFFEAF7F6)
private val BorderColor = Color(0xFFD7E5E3)
private val TitleGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val ActiveColor = Color(0xFF2E7D32)
private val InactiveColor = Color(0xFFB00020)
private val AdoptedColor = Color(0xFF1565C0)

private enum class PetType(val label: String) {
    TODOS("Todos"),
    PERRO("Perro"),
    GATO("Gato"),
    OTRO("Otro");

    companion object {
        fun fromValue(value: String?): PetType = values().firstOrNull {
            it.label.equals(value, ignoreCase = true)
        } ?: OTRO
    }
}

private enum class PetStatus(val label: String) {
    TODOS("Todos"),
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    ADOPTADO("Adoptado");

    companion object {
        fun fromValue(value: String?): PetStatus = values().firstOrNull {
            it.label.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: TODOS
    }
}

private enum class SortColumn {
    NAME,
    CREATED_AT,
    STATUS,
}

data class PerPageOption(val value: Int) {
    val label = value.toString()
}

@Composable
fun AdminMascotasScreen(
    onLogout: () -> Unit,
    onNavigateToEditPet: (String?) -> Unit,
) {
    val viewModel: AdminMascotasViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearch by remember { mutableStateOf("") }
    var speciesExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var perPageExpanded by remember { mutableStateOf(false) }
    var selectedSpecies by remember { mutableStateOf(PetType.TODOS) }
    var selectedStatus by remember { mutableStateOf(PetStatus.TODOS) }
    var breed by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(1) }
    var perPage by remember { mutableIntStateOf(10) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }
    var selectedPetId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var sortColumn by remember { mutableStateOf(SortColumn.CREATED_AT) }
    var sortAscending by remember { mutableStateOf(false) }

    val isCompact = LocalConfiguration.current.screenWidthDp < 620
    val pets = when (uiState) {
        is AdminMascotasUiState.Success -> (uiState as AdminMascotasUiState.Success).pets
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllPets(page = page, perPage = perPage)
    }

    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearch = searchQuery.trim()
    }

    LaunchedEffect(page, perPage, selectedSpecies) {
        val speciesParam = if (selectedSpecies == PetType.TODOS) "" else selectedSpecies.label
        viewModel.loadAllPets(page = page, perPage = perPage, species = speciesParam)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminMascotasUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((uiState as AdminMascotasUiState.Error).message)
                }
            }
            is AdminMascotasUiState.Success -> {
                if (successMessage.isNotBlank()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(successMessage)
                        successMessage = ""
                    }
                }
            }
            else -> {}
        }
    }

    val filteredPets = pets.filter { pet ->
        val matchesSearch = debouncedSearch.isBlank() || listOf(pet.id, pet.name, pet.species, pet.breed)
            .any { value -> value.contains(debouncedSearch, ignoreCase = true) }
        val matchesSpecies = selectedSpecies == PetType.TODOS || pet.species.equals(selectedSpecies.label, ignoreCase = true)
        val petStatus = PetStatus.fromValue(pet.status)
        val matchesStatus = selectedStatus == PetStatus.TODOS || petStatus == selectedStatus
        val matchesBreed = breed.isBlank() || pet.breed.contains(breed, ignoreCase = true)
        matchesSearch && matchesSpecies && matchesStatus && matchesBreed
    }

    val sortedPets = remember(filteredPets, sortColumn, sortAscending) {
        filteredPets.sortedWith(compareBy<PetResponse> {
            when (sortColumn) {
                SortColumn.NAME -> it.name.lowercase()
                SortColumn.CREATED_AT -> it.createdAt.orEmpty()
                SortColumn.STATUS -> PetStatus.fromValue(it.status).ordinal.toString()
            }
        }).let { if (sortAscending) it else it.reversed() }
    }

    Scaffold(
        topBar = { AdminTopBar(onLogout = onLogout) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    placeholder = { Text("Buscar por nombre, ID o raza") },
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
                    onOptionSelected = {
                        selectedSpecies = it
                        page = 1
                    },
                )

                DropdownChip(
                    label = "Estado",
                    value = selectedStatus.label,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onDismiss = { statusExpanded = false },
                    options = PetStatus.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = {
                        selectedStatus = it
                        page = 1
                    },
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    modifier = Modifier.width(160.dp),
                    placeholder = { Text("Raza") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                DropdownChip(
                    label = "Ítems",
                    value = "$perPage/pág",
                    expanded = perPageExpanded,
                    onExpandedChange = { perPageExpanded = it },
                    onDismiss = { perPageExpanded = false },
                    options = listOf(PerPageOption(10), PerPageOption(25), PerPageOption(50)),
                    optionLabel = { it.label },
                    onOptionSelected = {
                        perPage = it.value
                        page = 1
                    },
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            if (uiState is AdminMascotasUiState.Loading) {
                PetListSkeleton(isCompact = isCompact)
            } else if (uiState is AdminMascotasUiState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (uiState as AdminMascotasUiState.Error).message,
                        color = InactiveColor,
                    )
                }
            } else if (sortedPets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                        Text("No se encontraron mascotas", fontWeight = FontWeight.Bold)
                        Text("Ajusta tus filtros o agrega una mascota nueva.", color = GrayText)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                ) {
                    if (isCompact) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            sortedPets.forEachIndexed { index, pet ->
                                CompactPetRow(
                                    pet = pet,
                                    selected = selectedRowIndex == index,
                                    onClick = {
                                        selectedRowIndex = index
                                        selectedPetId = pet.id
                                        showDetailsDialog = true
                                        viewModel.loadPetDetails(pet.id)
                                    },
                                    onEdit = { onNavigateToEditPet(pet.id) },
                                    onDelete = {
                                        selectedRowIndex = index
                                        selectedPetId = pet.id
                                        showDeleteDialog = true
                                    },
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.width(780.dp)) {
                            TableHeader(
                                sortColumn = sortColumn,
                                ascending = sortAscending,
                                onSortRequested = { clicked ->
                                    if (sortColumn == clicked) sortAscending = !sortAscending else {
                                        sortColumn = clicked
                                        sortAscending = true
                                    }
                                },
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                            ) {
                                itemsIndexed(sortedPets) { index, pet ->
                                    PetFullRow(
                                        pet = pet,
                                        selected = selectedRowIndex == index,
                                        onClick = {
                                            selectedRowIndex = index
                                            selectedPetId = pet.id
                                            showDetailsDialog = true
                                            viewModel.loadPetDetails(pet.id)
                                        },
                                        onEdit = { onNavigateToEditPet(pet.id) },
                                        onDelete = {
                                            selectedRowIndex = index
                                            selectedPetId = pet.id
                                            showDeleteDialog = true
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = {
                    if (page > 1) page -= 1
                }, enabled = page > 1) {
                    Text(text = "Anterior")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Página $page")
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { page += 1 }) {
                    Text(text = "Siguiente")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Eliminar mascota") },
            text = { Text(text = "¿Seguro que deseas eliminar esta mascota?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedPetId?.let {
                        successMessage = "Mascota eliminada"
                        viewModel.deletePet(it)
                    }
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }

    if (showDetailsDialog) {
        PetDetailsDialog(
            pet = selectedPet,
            loading = selectedPet?.id != selectedPetId,
            onDismiss = { showDetailsDialog = false },
            onEdit = { onNavigateToEditPet(selectedPetId) },
            onDelete = {
                selectedPetId?.let {
                    successMessage = "Mascota eliminada"
                    viewModel.deletePet(it)
                    showDeleteDialog = false
                    showDetailsDialog = false
                }
            },
        )
    }
}

@Composable
private fun PetDetailsDialog(
    pet: PetResponse?,
    loading: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = pet?.name ?: "Detalles mascota") },
        text = {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow("ID", pet?.id.orEmpty())
                    DetailRow("Especie", pet?.species.orEmpty())
                    DetailRow("Raza", pet?.breed.orEmpty())
                    DetailRow("Estado", PetStatus.fromValue(pet?.status).label)
                    DetailRow("Fecha de nacimiento", pet?.dateOfBirth.orEmpty())
                    DetailRow("Fecha de registro", pet?.createdAt.orEmpty())
                    DetailRow("Notas", pet?.notes.orEmpty().ifBlank { "No hay historial disponible" })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) { Text("Editar") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDelete) { Text("Eliminar") }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = GrayText, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PetListSkeleton(isCompact: Boolean) {
    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { SkeletonCard() }
        }
    } else {
        Column(modifier = Modifier.width(780.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { SkeletonRow() }
        }
    }
}

@Composable
private fun SkeletonCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF0F0F0),
    ) {}
}

@Composable
private fun SkeletonRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp)
            .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.width(120.dp).height(16.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))) {}
    }
}

@Composable
private fun CompactPetRow(
    pet: PetResponse,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = PetStatus.fromValue(pet.status)
    val background = if (selected) TealSoft else Color.White
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(statusColor(status).copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = statusColor(status), modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(text = pet.name, fontWeight = FontWeight.SemiBold)
                    Text(text = pet.breed, color = GrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            StatusBadge(status = status)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
        }
    }
}

@Composable
private fun PetFullRow(
    pet: PetResponse,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = PetStatus.fromValue(pet.status)
    val background = if (selected) TealSoft else Color.White
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(text = pet.id.takeIf { it.length > 8 }?.let { "${it.take(8)}..." } ?: pet.id, width = 68.dp)
            TableCell(text = pet.name, width = 140.dp, bold = true)
            Row(modifier = Modifier.width(110.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Pets, contentDescription = null, tint = statusColor(status), modifier = Modifier.size(18.dp))
                Text(text = pet.species.ifBlank { "Desconocido" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TableCell(text = pet.breed, width = 140.dp)
            TableCell(text = pet.ownerId.ifBlank { "-" }, width = 140.dp)
            StatusBadge(status = status)
            TableCell(text = pet.createdAt?.take(10).orEmpty(), width = 100.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
            }
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderColor))
    }
}

@Composable
private fun StatusBadge(status: PetStatus) {
    val color = statusColor(status)
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text = status.label, color = color, fontWeight = FontWeight.SemiBold)
    }
}

private fun statusColor(status: PetStatus): Color = when (status) {
    PetStatus.ACTIVO -> ActiveColor
    PetStatus.INACTIVO -> InactiveColor
    PetStatus.ADOPTADO -> AdoptedColor
    else -> GrayText
}

@Composable
private fun TableHeader(
    sortColumn: SortColumn = SortColumn.CREATED_AT,
    ascending: Boolean = false,
    onSortRequested: (SortColumn) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, BorderColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(text = "ID", width = 68.dp)
        SortableHeaderCell(text = "Nombre", width = 140.dp, column = SortColumn.NAME, currentSort = sortColumn, ascending = ascending, onSortRequested = onSortRequested)
        HeaderCell(text = "Tipo", width = 110.dp)
        HeaderCell(text = "Raza", width = 140.dp)
        HeaderCell(text = "Dueño", width = 140.dp)
        SortableHeaderCell(text = "Estado", width = 100.dp, column = SortColumn.STATUS, currentSort = sortColumn, ascending = ascending, onSortRequested = onSortRequested)
        SortableHeaderCell(text = "Registro", width = 100.dp, column = SortColumn.CREATED_AT, currentSort = sortColumn, ascending = ascending, onSortRequested = onSortRequested)
        HeaderCell(text = "Acciones", width = 90.dp)
    }
}

@Composable
private fun RowScope.SortableHeaderCell(
    text: String,
    width: Dp,
    column: SortColumn,
    currentSort: SortColumn,
    ascending: Boolean,
    onSortRequested: (SortColumn) -> Unit,
) {
    val indicator = if (currentSort == column) if (ascending) "▲" else "▼" else ""
    Text(
        text = "$text $indicator",
        modifier = Modifier
            .width(width)
            .clickable { onSortRequested(column) },
        fontWeight = FontWeight.Bold,
        color = GrayText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
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
) {
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
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = GrayText)
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
    icon: ImageVector,
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

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onLogout) {
            Text(text = "Salir")
        }
    }
}
