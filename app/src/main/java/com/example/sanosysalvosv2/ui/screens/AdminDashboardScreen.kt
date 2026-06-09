package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        adminViewModel.loadUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text(text = "Dashboard Admin", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Usuarios registrados: ${adminViewModel.users.size}")

        adminViewModel.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }

        if (adminViewModel.loading) {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(text = "Refrescar", onClick = { adminViewModel.loadUsers() })
            }
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(text = "Cerrar sesion", onClick = onLogout)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(adminViewModel.users) { user ->
                Column {
                    Text(
                        text = "${user.fullName} | ${user.email} | ${user.role}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "ID: ${user.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}
