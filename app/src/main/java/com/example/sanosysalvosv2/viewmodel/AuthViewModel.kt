package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.repository.AuthRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.LoginRequest
import com.example.sanosysalvosv2.model.RegisterRequest
import com.example.sanosysalvosv2.util.ErrorHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var userRole by mutableStateOf("USER")
        private set

    init {
        viewModelScope.launch {
            isLoggedIn = !sessionStore.tokenFlow.first().isNullOrBlank()
            userRole = sessionStore.roleFlow.first()?.uppercase() ?: "USER"
        }
    }

    fun register(fullName: String, email: String, password: String) {
        error = null
        successMessage = null
        loading = true

        viewModelScope.launch {
            try {
                if (fullName.isBlank()) {
                    throw IllegalArgumentException("Nombre completo requerido")
                }
                if (email.isBlank() || !email.contains("@")) {
                    throw IllegalArgumentException("Correo inválido")
                }
                if (password.length < 6) {
                    throw IllegalArgumentException("Contraseña mínima de 6 caracteres")
                }

                val response = repository.register(
                    RegisterRequest(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        password = password,
                    )
                )
                sessionStore.saveSession(response.token, response.userId, response.role)
                isLoggedIn = true
                userRole = response.role.uppercase()
                showSuccessMessage("Registro exitoso")
            } catch (e: Exception) {
                error = ErrorHandler.getErrorMessage(e)
            } finally {
                loading = false
            }
        }
    }

    fun login(email: String, password: String) {
        error = null
        successMessage = null
        isLoggedIn = false
        loading = true

        viewModelScope.launch {
            try {
                if (email.isBlank() || password.isBlank()) {
                    throw IllegalArgumentException("Correo y contraseña son requeridos")
                }

                sessionStore.clearSession()

                val response = repository.login(
                    LoginRequest(
                        email = email.trim(),
                        password = password,
                    )
                )
                sessionStore.saveSession(response.token, response.userId, response.role)
                isLoggedIn = true
                userRole = response.role.uppercase()
                showSuccessMessage("Sesión iniciada exitosamente")
            } catch (e: Exception) {
                sessionStore.clearSession()
                isLoggedIn = false
                userRole = "USER"
                error = withDebugCredentialHint(ErrorHandler.getErrorMessage(e))
            } finally {
                loading = false
            }
        }
    }

    private fun withDebugCredentialHint(message: String): String {
        return message
    }

    private fun showSuccessMessage(message: String) {
        successMessage = message
        viewModelScope.launch {
            delay(2000) // Mostrar por 2 segundos
            successMessage = null
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionStore.clearSession()
            successMessage = null
            error = null
            isLoggedIn = false
            userRole = "USER"
        }
    }
}
