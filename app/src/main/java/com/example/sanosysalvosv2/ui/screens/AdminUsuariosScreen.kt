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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.sanosysalvosv2.viewmodel.AdminViewModel

private val Teal = Color(0xFF0F8A8A)
private val TealDark = Color(0xFF0F5B5B)
private val TealSoft = Color(0xFFE8F7F6)
private val Border = Color(0xFFD7E5E3)
private val TitleGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val PendingOrange = Color(0xFFE08A18)
private val BlockedRed = Color(0xFFC53B3B)

private data class AdminUserRowMock(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val status: UserStatus,
)

private enum class UserRole(val label: String) {
    TODOS("Todos"),
    USUARIO("Usuario"),
    ENTIDAD("Entidad"),
    ADMIN("Admin"),
}

private enum class UserStatus(val label: String) {
    TODOS("Todos"),
    ACTIVO("Activo"),
    PENDIENTE("Pendiente"),
    BLOQUEADO("Bloqueado"),
}

@Composable
fun AdminUsuariosScreen(
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        adminViewModel.loadUsers()
    }

    val mockUsers = remember {
        listOf(
            AdminUserRowMock("USR-001", "Camila Orellana", "camila.orellana@mail.com", "+56 9 7123 4567", UserRole.USUARIO, UserStatus.ACTIVO),
            AdminUserRowMock("USR-002", "Valentina Perez", "valentina.perez@mail.com", "+56 9 8234 5678", UserRole.USUARIO, UserStatus.PENDIENTE),
            AdminUserRowMock("USR-003", "Carlos Gómez", "carlos.gomez@mail.com", "+56 9 9345 6789", UserRole.ADMIN, UserStatus.ACTIVO),
            AdminUserRowMock("ENT-004", "Clinica VitPatas", "contacto@vitpatas.cl", "+56 2 2345 6789", UserRole.ENTIDAD, UserStatus.ACTIVO),
            AdminUserRowMock("ENT-005", "Refugio Huellas", "hola@refugiohuellas.cl", "+56 9 3456 7890", UserRole.ENTIDAD, UserStatus.PENDIENTE),
            AdminUserRowMock("USR-006", "José Muñoz", "jose.munoz@mail.com", "+56 9 4567 8901", UserRole.USUARIO, UserStatus.BLOQUEADO),
            AdminUserRowMock("ENT-007", "Municipalidad Ñuñoa", "contacto@muninunoa.cl", "+56 2 3456 7890", UserRole.ENTIDAD, UserStatus.ACTIVO),
        )
    }

    var query by remember { mutableStateOf("") }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.TODOS) }
    var selectedStatus by remember { mutableStateOf(UserStatus.TODOS) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }

    val filteredUsers = mockUsers.filter { user ->
        val matchesQuery = query.isBlank() || listOf(user.id, user.name, user.email, user.phone)
            .any { value -> value.contains(query, ignoreCase = true) }
        val matchesRole = selectedRole == UserRole.TODOS || user.role == selectedRole
        val matchesStatus = selectedStatus == UserStatus.TODOS || user.status == selectedStatus
        matchesQuery && matchesRole && matchesStatus
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
                    placeholder = { Text("Buscar usuario...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                Button(
                    onClick = { /* placeholder */ },
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
                    options = UserRole.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { selectedRole = it }
                )
                DropdownChip(
                    label = "Estado",
                    value = selectedStatus.label,
                    expanded = statusMenuExpanded,
                    onExpandedChange = { statusMenuExpanded = it },
                    onDismiss = { statusMenuExpanded = false },
                    options = UserStatus.entries,
                    optionLabel = { it.label },
                    onOptionSelected = { selectedStatus = it }
                )

                Spacer(modifier = Modifier.weight(1f))

                CircleActionButton(
                    icon = Icons.Filled.Delete,
                    onClick = { /* placeholder */ },
                )
                CircleActionButton(
                    icon = Icons.Filled.Edit,
                    onClick = { /* placeholder */ },
                )
            }

            if (adminViewModel.loading) {
                Text(text = "Cargando usuarios reales desde el backend...", color = GrayText)
            }
            adminViewModel.error?.let {
                Text(text = it, color = BlockedRed)
            }

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
                        onClick = { selectedRowIndex = rowIndex },
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
            .border(1.dp, Border)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell(text = "ID", weight = 0.9f)
        HeaderCell(text = "Nombre", weight = 1.8f)
        HeaderCell(text = "Correo", weight = 2.2f)
        HeaderCell(text = "Fono", weight = 1.3f)
        HeaderCell(text = "Rol", weight = 1.0f)
        HeaderCell(text = "Estado", weight = 1.0f)
    }
}

@Composable
private fun UserTableRow(
    user: AdminUserRowMock,
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
            TableCell(text = user.id, weight = 0.9f)
            TableCell(text = user.name, weight = 1.8f, bold = true)
            TableCell(text = user.email, weight = 2.2f)
            TableCell(text = user.phone, weight = 1.3f)
            TableCell(text = user.role.label, weight = 1.0f)
            TableCell(
                text = user.status.label,
                weight = 1.0f,
                color = statusColor(user.status),
                bold = true,
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
    weight: Float,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        color = GrayText,
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
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(Teal, RoundedCornerShape(999.dp))
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
        androidx.compose.material3.TextButton(onClick = onLogout) {
            Text(text = "Salir")
        }
    }
}
