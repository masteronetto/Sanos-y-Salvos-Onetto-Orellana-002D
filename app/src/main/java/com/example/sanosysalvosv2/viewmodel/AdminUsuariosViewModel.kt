package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminUsersRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminCreateUserRequest
import com.example.sanosysalvosv2.model.AdminUserSummary
import com.example.sanosysalvosv2.util.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AdminUsuariosUiState {
    object Loading : AdminUsuariosUiState
    data class Success(val users: List<AdminUserSummary>) : AdminUsuariosUiState
    data class Error(val message: String) : AdminUsuariosUiState
}

class AdminUsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminUsersRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    private val _uiState = MutableStateFlow<AdminUsuariosUiState>(AdminUsuariosUiState.Loading)
    val uiState: StateFlow<AdminUsuariosUiState> = _uiState.asStateFlow()

    private var currentUsers: List<AdminUserSummary> = emptyList()

    fun loadUsers() {
        _uiState.value = AdminUsuariosUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                currentUsers = repository.listUsers(token)
                _uiState.value = AdminUsuariosUiState.Success(currentUsers)
            } catch (e: Exception) {
                Log.e("AdminUsuariosVM", "loadUsers failed", e)
                _uiState.value = AdminUsuariosUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun createUser(request: AdminCreateUserRequest) {
        _uiState.value = AdminUsuariosUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                repository.createUser(token, request)
                loadUsers()
            } catch (e: Exception) {
                Log.e("AdminUsuariosVM", "createUser failed", e)
                _uiState.value = AdminUsuariosUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun deleteUser(userId: String) {
        _uiState.value = AdminUsuariosUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                val deleted = repository.deleteUser(token, userId)
                if (!deleted) throw IllegalStateException("No se pudo eliminar el usuario")
                loadUsers()
            } catch (e: Exception) {
                Log.e("AdminUsuariosVM", "deleteUser failed", e)
                _uiState.value = AdminUsuariosUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }

    fun updateUserStatus(userId: String, status: String) {
        _uiState.value = AdminUsuariosUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionStore.tokenFlow.first()
                if (token.isNullOrBlank()) throw IllegalStateException("Token de sesión inválido")

                repository.updateUser(token, userId, mapOf("status" to status))
                loadUsers()
            } catch (e: Exception) {
                Log.e("AdminUsuariosVM", "updateUserStatus failed", e)
                _uiState.value = AdminUsuariosUiState.Error(ErrorHandler.getErrorMessage(e))
            }
        }
    }
}
