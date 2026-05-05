package com.sanosysalvos.androidapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanosysalvos.androidapp.data.repository.AuthRepository
import com.sanosysalvos.androidapp.model.LoginRequest
import com.sanosysalvos.androidapp.model.RegisterRequest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun register(email: String, password: String) {
        error = null
        successMessage = null
        loading = true
        viewModelScope.launch {
            try {
                // Validación básica
                if (email.isBlank() || password.length < 6) {
                    throw IllegalArgumentException("Correo inválido o contraseña muy corta")
                }
                val req = RegisterRequest(email = email.trim(), password = password)
                val resp = repository.register(req)
                successMessage = "Registrado. userId=${resp.userId}"
            } catch (e: Exception) {
                error = e.message ?: "Error desconocido"
            } finally {
                loading = false
            }
        }
    }

    fun login(email: String, password: String) {
        error = null
        successMessage = null
        loading = true
        viewModelScope.launch {
            try {
                if (email.isBlank() || password.isBlank()) {
                    throw IllegalArgumentException("Correo y contraseña son requeridos")
                }
                val req = LoginRequest(email = email.trim(), password = password)
                val resp = repository.login(req)
                successMessage = "Sesión iniciada. token=${resp.token}"
            } catch (e: Exception) {
                error = e.message ?: "Error desconocido"
            } finally {
                loading = false
            }
        }
    }
}
