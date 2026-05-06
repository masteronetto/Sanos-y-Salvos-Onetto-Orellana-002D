package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.components.PrimaryButton
import com.example.sanosysalvosv2.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAdmin: () -> Unit,
) {
    val fullName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    LaunchedEffect(authViewModel.isLoggedIn) {
        if (authViewModel.isLoggedIn) {
            if (authViewModel.userRole == "ADMIN") {
                onNavigateToAdmin()
            } else {
                onNavigateToHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Registro", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fullName.value,
            onValueChange = { fullName.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Nombre completo") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Correo") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Contrasena (min 6)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (authViewModel.loading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
        }

        authViewModel.error?.let {
            Text(text = it, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        authViewModel.successMessage?.let {
            Text(text = it)
            Spacer(modifier = Modifier.height(8.dp))
        }

        PrimaryButton(
            text = "Registrarse",
            enabled = !authViewModel.loading,
            onClick = {
                authViewModel.register(
                    fullName = fullName.value,
                    email = email.value,
                    password = password.value,
                )
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = "Volver a login",
            enabled = !authViewModel.loading,
            onClick = onNavigateToLogin,
        )
    }
}
