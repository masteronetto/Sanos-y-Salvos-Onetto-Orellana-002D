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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.model.AdminCreateUserRequest
import com.example.sanosysalvosv2.model.AdminUserSummary
import com.example.sanosysalvosv2.viewmodel.AdminUsuariosUiState
import com.example.sanosysalvosv2.viewmodel.AdminUsuariosViewModel

private val Teal = Color(0xFF0F8A8A)
private val TealSoft = Color(0xFFE8F7F6)
private val Border = Color(0xFFD7E5E3)
private val TitleGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val PendingOrange = Color(0xFFE08A18)
private val BlockedRed = Color(0xFFC53B3B)

private enum class UserRole(val label: String) {
    TODOS("Todos"),
    USUARIO("Usuario"),
    ENTIDAD("Entidad"),
    ADMIN("Admin");

    companion object {
        fun fromRole(role: String): UserRole = values().firstOrNull { it.name == role.uppercase() } ?: USUARIO
    }
}

private enum class UserStatus(val label: String) {
    TODOS("Todos"),
    ACTIVO("Activo"),
    PENDIENTE("Pendiente"),
    BLOQUEADO("Bloqueado");

    companion object {
        fun fromStatus(status: String): UserStatus = values().firstOrNull { it.name == status.uppercase() } ?: PENDIENTE
    }
}

@Composable
fun AdminUsuariosScreen(
    onLogout: () -> Unit,
) {
    val adminUsuariosViewModel: AdminUsuariosViewModel = viewModel()
    LaunchedEffect(Unit) {
        adminUsuariosViewModel.loadUsers()
    }

    val uiState by adminUsuariosViewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.TODOS) }
    var selectedStatus by remember { mutableStateOf(UserStatus.TODOS) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var statusUpdateMenuExpanded by remember { mutableStateOf(false) }

    val users = when (uiState) {
        is AdminUsuariosUiState.Success -> (uiState as AdminUsuariosUiState.Success).users
        else -> emptyList()
    }

    val filteredUsers = users.filter { user ->
        val matchesQuery = query.isBlank() || listOf(user.fullName, user.email)
            .any { value -> value.contains(query, ignoreCase = true) }
        val userRole = UserRole.fromRole(user.role)
        val userStatus = UserStatus.fromStatus(user.status)
        val matchesRole = selectedRole == UserRole.TODOS || userRole == selectedRole
        val matchesStatus = selectedStatus == UserStatus.TODOS || userStatus == selectedStatus
        matchesQuery && matchesRole && matchesStatus
    }

    androidx.compose.material3.Scaffold(
        topBar = { AdminTopBar(onLogout = onLogout) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Usuarios",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TitleGreen,
                )
                Text(
                    text = "Administra a los usuarios registrados en la plataforma",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayText,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar por nombre o email") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(text = "+ Nuevo usuario", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DropdownChip(
                        label = "Rol",
                        value = selectedRole.label,
                        expanded = roleMenuExpanded,
                        onExpandedChange = { roleMenuExpanded = it },
                        onDismiss = { roleMenuExpanded = false },
                        options = UserRole.values().toList(),
                        optionLabel = { it.label },
                        onOptionSelected = { selectedRole = it },
                    )
                    DropdownChip(
                        label = "Estado",
                        value = selectedStatus.label,
                        expanded = statusMenuExpanded,
                        onExpandedChange = { statusMenuExpanded = it },
                        onDismiss = { statusMenuExpanded = false },
                        options = UserStatus.values().toList(),
                        optionLabel = { it.label },
                        onOptionSelected = { selectedStatus = it },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CircleActionButton(
                        icon = Icons.Filled.Delete,
                        onClick = { showDeleteDialog = true },
                        enabled = selectedUserId != null,
                    )
                    Box {
                        CircleActionButton(
                            icon = Icons.Filled.Edit,
                            onClick = { statusUpdateMenuExpanded = true },
                            enabled = selectedUserId != null,
                        )
                        DropdownMenu(
                            expanded = statusUpdateMenuExpanded,
                            onDismissRequest = { statusUpdateMenuExpanded = false },
                        ) {
                            UserStatus.values()
                                .filter { it != UserStatus.TODOS }
                                .forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption.label) },
                                        onClick = {
                                            selectedUserId?.let { adminUsuariosViewModel.updateUserStatus(it, statusOption.name) }
                                            statusUpdateMenuExpanded = false
                                        },
                                    )
                                }
                        }
                    }
                }

                when (uiState) {
                    is AdminUsuariosUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is AdminUsuariosUiState.Error -> {
                        Text(
                            text = (uiState as AdminUsuariosUiState.Error).message,
                            color = BlockedRed,
                        )
                    }
                    is AdminUsuariosUiState.Success -> {
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
                                    items(filteredUsers) { user ->
                                        val rowIndex = filteredUsers.indexOf(user)
                                        UserTableRow(
                                            user = user,
                                            selected = selectedRowIndex == rowIndex,
                                            onClick = {
                                                selectedRowIndex = rowIndex
                                                selectedUserId = user.id
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showCreateDialog) {
                AdminCreateUserDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreate = { request ->
                        adminUsuariosViewModel.createUser(request)
                        showCreateDialog = false
                    },
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(text = "Eliminar usuario") },
                    text = { Text(text = "¿Seguro que deseas eliminar este usuario?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedUserId?.let { adminUsuariosViewModel.deleteUser(it) }
                                showDeleteDialog = false
                            },
                        ) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                    },
                )
            }
        }
    }
}

@Composable
private fun AdminCreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (AdminCreateUserRequest) -> Unit,
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.USUARIO) }
    var status by remember { mutableStateOf(UserStatus.ACTIVO) }
    var roleExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nuevo usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownChip(
                    label = "Rol",
                    value = role.label,
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it },
                    onDismiss = { roleExpanded = false },
                    options = listOf(UserRole.USUARIO, UserRole.ENTIDAD, UserRole.ADMIN),
                    optionLabel = { it.label },
                    onOptionSelected = { role = it },
                )
                DropdownChip(
                    label = "Estado",
                    value = status.label,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onDismiss = { statusExpanded = false },
                    options = listOf(UserStatus.ACTIVO, UserStatus.PENDIENTE, UserStatus.BLOQUEADO),
                    optionLabel = { it.label },
                    onOptionSelected = { status = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        AdminCreateUserRequest(
                            fullName = fullName,
                            email = email,
                            password = password,
                            phone = phone,
                            role = role.name,
                            status = status.name,
                        ),
                    )
                },
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .width(518.dp)
            .background(Color.White)
            .border(1.dp, Border)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell(text = "ID", width = 48.dp)
        HeaderCell(text = "Nombre", width = 120.dp)
        HeaderCell(text = "Correo", width = 140.dp)
        HeaderCell(text = "Fono", width = 90.dp)
        HeaderCell(text = "Rol", width = 70.dp)
        HeaderCell(text = "Estado", width = 70.dp)
    }
}

@Composable
private fun UserTableRow(
    user: AdminUserSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) TealSoft else Color.White
    val userRole = UserRole.fromRole(user.role)
    val userStatus = UserStatus.fromStatus(user.status)

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
            TableCell(text = user.id, width = 48.dp)
            TableCell(text = user.fullName, width = 120.dp, bold = true)
            TableCell(text = user.email, width = 140.dp)
            TableCell(text = user.phone, width = 90.dp)
            TableCell(text = userRole.label, width = 70.dp)
            TableCell(
                text = userStatus.label,
                width = 70.dp,
                color = statusColor(userStatus),
                bold = true,
                overflow = TextOverflow.Clip,
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border),
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
                .border(1.dp, Border, RoundedCornerShape(999.dp))
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "$label: $value", color = Color.Black, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = GrayText,
            )
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
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .background(if (enabled) Teal else Teal.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .padding(2.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

private fun statusColor(status: UserStatus): Color = when (status) {
    UserStatus.ACTIVO -> Teal
    UserStatus.PENDIENTE -> PendingOrange
    UserStatus.BLOQUEADO -> BlockedRed
    UserStatus.TODOS -> Color.Black
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


