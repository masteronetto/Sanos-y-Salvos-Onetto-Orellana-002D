package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminUserSummary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var users by mutableStateOf<List<AdminUserSummary>>(emptyList())
        private set

    fun loadUsers() {
        loading = true
        error = null

        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    throw IllegalStateException("Sesion invalida")
                }
                users = repository.listRegisteredUsers(token)
            } catch (e: Exception) {
                error = e.message ?: "No se pudo cargar usuarios"
            } finally {
                loading = false
            }
        }
    }
}
