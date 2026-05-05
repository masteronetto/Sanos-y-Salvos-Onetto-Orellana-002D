package com.sanosysalvos.androidapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanosysalvos.androidapp.ui.components.PrimaryButton
import com.sanosysalvos.androidapp.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(authViewModel: AuthViewModel, onNavigateToLogin: () -> Unit) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Registro")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Correo") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Contraseña (min 6)") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (authViewModel.loading) {
            CircularProgressIndicator()
        }

        authViewModel.error?.let { err ->
            Text(text = err, modifier = Modifier.padding(8.dp), color = androidx.compose.ui.graphics.Color.Red)
        }

        authViewModel.successMessage?.let { msg ->
            Text(text = msg, modifier = Modifier.padding(8.dp))
        }

        PrimaryButton(text = "Registrarse", onClick = { authViewModel.register(email.value, password.value) })

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(text = "Volver a login", onClick = onNavigateToLogin)
    }
}
