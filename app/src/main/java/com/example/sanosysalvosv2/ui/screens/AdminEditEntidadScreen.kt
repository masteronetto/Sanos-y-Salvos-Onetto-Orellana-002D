package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// Removed KeyboardOptions/KeyboardType imports (not used)
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanosysalvosv2.model.CollaboratorRequest
import com.example.sanosysalvosv2.model.CollaboratorResponse
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.viewmodel.AdminEntidadesUiState
import com.example.sanosysalvosv2.viewmodel.AdminEntidadesViewModel

private val Teal = Color(0xFF0F8A8A)
private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val ErrorRed = Color(0xFFD32F2F)

private enum class EntidadType(val label: String, val value: String) {
    VETERINARY_CLINIC("Clínica", "VETERINARY_CLINIC"),
    REFUGIO("Refugio", "SHELTER"),
    MUNICIPALIDAD("Municipalidad", "MUNICIPALITY"),
    VOLUNTARIO("Voluntario", "VOLUNTEER"),
}

private enum class EntidadStatus(val label: String, val value: String) {
    ACTIVO("Activo", "ACTIVE"),
    PENDIENTE("Pendiente", "PENDING"),
    BLOQUEADO("Bloqueado", "BLOCKED"),
}

@Composable
fun AdminEditEntidadScreen(
    entidadId: String?,
    onNavigateBack: () -> Unit,
) {
    val viewModel: AdminEntidadesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedEntidad by viewModel.selectedEntidad.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEntidades()
        entidadId?.let { viewModel.loadEntidadDetail(it) }
    }

    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(EntidadType.VETERINARY_CLINIC.value) }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(EntidadStatus.ACTIVO.value) }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(selectedEntidad) {
        selectedEntidad?.let { entity ->
            nombre = entity.name
            tipo = entity.type
            email = entity.email
            telefono = entity.phone
            comuna = entity.comuna
            direccion = entity.address.orEmpty()
            estado = when (entity.status.uppercase()) {
                EntidadStatus.ACTIVO.value -> EntidadStatus.ACTIVO.value
                EntidadStatus.PENDIENTE.value -> EntidadStatus.PENDIENTE.value
                else -> EntidadStatus.BLOQUEADO.value
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminEntidadesUiState.Error -> {
                message = (uiState as AdminEntidadesUiState.Error).message
                saving = false
            }
            is AdminEntidadesUiState.Success -> {
                if (saving) {
                    saving = false
                    onNavigateBack()
                }
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(4.dp),
                    tint = DarkGreen,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (entidadId == null) "Nueva entidad" else "Editar entidad",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkGreen,
                )
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            DropdownField(
                label = "Tipo",
                selected = EntidadType.values().firstOrNull { it.value == tipo }?.label.orEmpty(),
                options = EntidadType.values().map { it.label },
                onSelected = { selectedLabel ->
                    tipo = EntidadType.values().firstOrNull { it.label == selectedLabel }?.value ?: tipo
                },
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = comuna,
                onValueChange = { comuna = it },
                label = { Text("Comuna") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            DropdownField(
                label = "Estado",
                selected = EntidadStatus.values().firstOrNull { it.value == estado }?.label.orEmpty(),
                options = EntidadStatus.values().map { it.label },
                onSelected = { selectedLabel ->
                    estado = EntidadStatus.values().firstOrNull { it.label == selectedLabel }?.value ?: estado
                },
            )

            if (message.isNotBlank()) {
                Text(text = message, color = ErrorRed)
            }

            when (uiState) {
                is AdminEntidadesUiState.Loading -> CircularProgressIndicator()
                else -> Button(
                    onClick = {
                        message = ""
                        saving = true
                        val request = CollaboratorRequest(
                            name = nombre.trim(),
                            type = tipo,
                            email = email.trim(),
                            phone = telefono.trim(),
                            comuna = comuna.trim(),
                            address = direccion.trim(),
                            status = estado,
                        )

                        if (entidadId == null) {
                            viewModel.createEntidad(request)
                        } else {
                            viewModel.updateEntidad(entidadId, request)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Guardar",
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
