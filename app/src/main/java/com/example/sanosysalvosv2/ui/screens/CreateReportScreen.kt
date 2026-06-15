package com.example.sanosysalvosv2.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sanosysalvosv2.model.ReportFormState
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.ui.screens.ReportFormContent
import com.example.sanosysalvosv2.viewmodel.ProfileUiState
import com.example.sanosysalvosv2.viewmodel.ProfileViewModel
import com.example.sanosysalvosv2.viewmodel.UserReportsUiState
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateReportScreen(
    navController: NavController,
    viewModel: UserReportsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var formState by remember { mutableStateOf(ReportFormState()) }
    var photoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            coroutineScope.launch {
                getLastKnownLocation(context)?.let { location ->
                    formState = formState.copy(latitude = location.latitude, longitude = location.longitude)
                }
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            photoUri = it
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) { uriToBase64(context, it) }
                formState = formState.copy(photoBase64 = base64)
            }
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            coroutineScope.launch {
                getLastKnownLocation(context)?.let { location ->
                    formState = formState.copy(latitude = location.latitude, longitude = location.longitude)
                }
            }
        } else if (!permissionRequested) {
            permissionRequested = true
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UserReportsUiState.Created -> navController.popBackStack()
            is UserReportsUiState.Error -> {
                val message = (uiState as UserReportsUiState.Error).message
                snackbarHostState.showSnackbar(message)
            }
            else -> {}
        }
    }

    val contactName = if (profileUiState is ProfileUiState.Success) {
        (profileUiState as ProfileUiState.Success).profile.fullName
    } else {
        "Usuario"
    }
    val contactPhone = if (profileUiState is ProfileUiState.Success) {
        (profileUiState as ProfileUiState.Success).profile.phone
    } else {
        "No disponible"
    }

    val isSubmitting = uiState is UserReportsUiState.Loading

    ReportFormContent(
        title = "Crear reporte",
        buttonText = "Enviar reporte",
        state = formState,
        onStateChange = { formState = it },
        contactName = contactName,
        contactPhone = contactPhone,
        photoUri = photoUri,
        onPickPhoto = { photoPicker.launch("image/*") },
        onUpdateLocation = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                coroutineScope.launch {
                    getLastKnownLocation(context)?.let { location ->
                        formState = formState.copy(latitude = location.latitude, longitude = location.longitude)
                    }
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        },
        onSubmit = {
            validationError = when {
                formState.description.isBlank() -> "Por favor ingresa una descripción del reporte."
                formState.eventDate.isBlank() -> "Por favor ingresa la fecha del evento."
                formState.latitude == 0.0 && formState.longitude == 0.0 -> "No se pudo obtener la ubicación. Actualiza la ubicación antes de enviar."
                else -> null
            }
            if (validationError == null) {
                viewModel.createReport(
                    ReportRequest(
                        type = formState.type,
                        description = formState.description.trim(),
                        latitude = formState.latitude,
                        longitude = formState.longitude,
                        locationName = formState.locationName.takeIf { it.isNotBlank() },
                        eventDate = formState.eventDate.trim(),
                        photoUrl = null,
                        photoBase64 = formState.photoBase64.takeIf { it.isNotBlank() },
                        species = formState.selectedSpeciesValue.takeIf { it.isNotBlank() },
                        breed = formState.breed.takeIf { it.isNotBlank() },
                        color = formState.color.takeIf { it.isNotBlank() },
                        petName = formState.petName.takeIf { it.isNotBlank() },
                        size = formState.selectedSizeValue.takeIf { it.isNotBlank() },
                    ),
                )
            }
        },
        isSubmitting = isSubmitting,
        validationError = validationError,
        errorMessage = (uiState as? UserReportsUiState.Error)?.message,
        onBack = { navController.popBackStack() },
    )
}

private suspend fun getLastKnownLocation(context: Context): Location? = withContext(Dispatchers.IO) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@withContext null
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )
    providers.asSequence().mapNotNull {
        try {
            locationManager.getLastKnownLocation(it)
        } catch (_: SecurityException) {
            null
        }
    }.firstOrNull()
}

private fun uriToBase64(context: Context, uri: Uri): String = try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        android.util.Base64.encodeToString(stream.readBytes(), android.util.Base64.NO_WRAP)
    } ?: ""
} catch (_: Exception) {
    ""
}
