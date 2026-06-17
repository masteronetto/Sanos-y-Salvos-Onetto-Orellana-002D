package com.example.sanosysalvosv2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanosysalvosv2.data.repository.AdminStatsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminDashboardStats
import com.example.sanosysalvosv2.model.RecoveryStats
import com.example.sanosysalvosv2.model.ReportsPerCommune
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdminDashboardNewViewModel(
    private val sessionStore: SessionStore,
    private val statsRepo: AdminStatsRepository
) : ViewModel() {

    private val _dashboardStats = MutableStateFlow<AdminDashboardStats?>(null)
    val dashboardStats: StateFlow<AdminDashboardStats?> = _dashboardStats.asStateFlow()

    private val _recoveryStats = MutableStateFlow<RecoveryStats?>(null)
    val recoveryStats: StateFlow<RecoveryStats?> = _recoveryStats.asStateFlow()

    private val _reportsByCommune = MutableStateFlow<List<ReportsPerCommune>>(emptyList())
    val reportsByCommune: StateFlow<List<ReportsPerCommune>> = _reportsByCommune.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                val token = sessionStore.tokenFlow.first() ?: ""
                val dashDeferred = async { statsRepo.fetchDashboard(token) }
                val recDeferred = async { statsRepo.fetchRecovery(token) }
                val communeDeferred = async { statsRepo.fetchReportsByCommune(token) }

                dashDeferred.await().onSuccess { _dashboardStats.value = it }
                recDeferred.await().onSuccess { _recoveryStats.value = it }
                communeDeferred.await().onSuccess { _reportsByCommune.value = it }

                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object Success : LoadingState()
    data class Error(val message: String) : LoadingState()
}
