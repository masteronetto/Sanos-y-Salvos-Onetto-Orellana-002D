package com.example.sanosysalvosv2.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.model.ReportTypeMapper
import com.example.sanosysalvosv2.viewmodel.ProfileUiState
import com.example.sanosysalvosv2.viewmodel.ProfileViewModel
import com.example.sanosysalvosv2.viewmodel.UserReportsUiState
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }

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
        uri?.let { selectedUri ->
            photoUri = selectedUri
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            val maxDim = 600
                            val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height, 1.0f)
                            val resized = if (scale < 1f) {
                                Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                            } else bitmap

                            val out = ByteArrayOutputStream()
                            resized.compress(Bitmap.CompressFormat.JPEG, 70, out)
                            val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                            
                            withContext(Dispatchers.Main) {
                                formState = formState.copy(photoBase64 = base64)
                            }

                            if (resized != bitmap) resized.recycle()
                            bitmap.recycle()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CreateReport", "Error processing photo: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("DEBUG_CREATE", "Screen opened. Initial uiState: ${viewModel.uiState.value}")
        viewModel.resetState()
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
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
                    when {
                        formState.description.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa una descripción del reporte.")
                            }
                        }
                        formState.eventDate.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa la fecha del evento.")
                            }
                        }
                        formState.locationName.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa el nombre del lugar.")
                            }
                        }
                        formState.selectedSpeciesValue.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Por favor selecciona la especie.")
                            }
                        }
                        formState.photoBase64.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Por favor agrega una foto para el reporte.")
                            }
                        }
                        (formState.latitude == 0.0 && formState.longitude == 0.0) -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("No se pudo obtener la ubicación. Actualiza la ubicación antes de enviar.")
                            }
                        }
                        else -> {
                            coroutineScope.launch {
                                val mappedType = ReportTypeMapper.displayToDb(formState.type) ?: "LOST"
                                
                                // Round to 4 decimals to match backend trace exactly
                                val lat = "%.4f".format(java.util.Locale.US, formState.latitude).toDouble()
                                val lng = "%.4f".format(java.util.Locale.US, formState.longitude).toDouble()

                                var photoBase64 = formState.photoBase64.takeIf { it.isNotBlank() }
                                    ?.replaceFirst(Regex("^data:image/[^;]+;base64,"), "")

                                if (photoBase64 != null && photoBase64.length > 400_000) {
                                    Log.w("DEBUG_PHOTO", "Photo too large: ${photoBase64.length} chars, recompressing")
                                    photoBase64 = compressBase64Photo(photoBase64)
                                    if (photoBase64 == null || photoBase64.length > 400_000) {
                                        snackbarHostState.showSnackbar("La foto es demasiado grande para enviar. Selecciona otra foto.")
                                        return@launch
                                    }
                                }

                                val request = ReportRequest(
                                    type = mappedType,
                                    description = formState.description.trim(),
                                    latitude = lat,
                                    longitude = lng,
                                    locationName = formState.locationName.trim().takeIf { it.isNotBlank() },
                                    eventDate = formState.eventDate.trim(),
                                    photoBase64 = photoBase64,
                                    species = formState.selectedSpeciesValue.takeIf { it.isNotBlank() },
                                    breed = formState.breed.trim().takeIf { it.isNotBlank() },
                                    color = formState.color.trim().takeIf { it.isNotBlank() },
                                    petId = formState.petId?.takeIf { it.isNotBlank() },
                                    petName = formState.petName.takeIf { it.isNotBlank() },
                                    size = formState.selectedSizeValue.takeIf { it.isNotBlank() }
                                )
                                viewModel.createReport(request)
                            }
                        }
                    }
                },
                isSubmitting = isSubmitting,
                validationError = null,
                errorMessage = (uiState as? UserReportsUiState.Error)?.message,
                onBack = { navController.popBackStack() },
            )
        }
    }
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

private suspend fun compressBase64Photo(base64: String, maxSize: Int = 400_000): String? = withContext(Dispatchers.IO) {
    try {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
        val outputStream = ByteArrayOutputStream()

        var quality = 50
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var compressed = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        while (compressed.length > maxSize && quality > 10) {
            quality -= 10
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressed = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }

        bitmap.recycle()
        if (compressed.length > maxSize) null else compressed
    } catch (e: Exception) {
        Log.e("CreateReport", "Photo recompression failed: ${e.message}")
        null
    }
}
