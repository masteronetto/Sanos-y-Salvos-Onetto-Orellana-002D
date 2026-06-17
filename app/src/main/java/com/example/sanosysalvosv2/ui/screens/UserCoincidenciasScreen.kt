package com.example.sanosysalvosv2.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sanosysalvosv2.model.UserMatchResponse
import com.example.sanosysalvosv2.ui.theme.Borders
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.UserCoincidenciasViewModel

private val Teal = Color(0xFF1D9E75)
private val TealLight = Color(0xFFEAF7F6)
private val Orange = Color(0xFFEF9F27)
private val Red = Color(0xFFE24B4A)
private val Gray = Color(0xFF707070)
private val ConfirmedGreen = Color(0xFF1D9E75)
private val RejectedBackground = Color(0xFFFFEBEE)
private val ConfirmedBackground = Color(0xFFE8F6EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCoincidenciasScreen(
    onBack: () -> Unit,
    onDetailClick: (String) -> Unit,
    onContactClick: (String) -> Unit = {},
    viewModel: UserCoincidenciasViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.loadMatches(refresh = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Coincidencias",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Teal,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
        containerColor = Color(0xFFF7F7F7),
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(Color(0xFFF7F7F7)),
        ) {
            when {
                viewModel.loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Teal)
                    }
                }
                viewModel.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = viewModel.error.orEmpty(),
                            color = Red,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadMatches(refresh = true) }, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                            Text(text = "Reintentar")
                        }
                    }
                }
                viewModel.matches.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No se encontraron coincidencias por ahora.",
                            color = Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(viewModel.matches) { match ->
                            MatchCard(
                                match = match,
                                onDetailClick = { onDetailClick(match.id) },
                                onContactClick = { onContactClick(match.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: UserMatchResponse,
    onDetailClick: () -> Unit,
    onContactClick: () -> Unit,
) {
    val score = match.score ?: 0
    val (levelLabel, levelColor, levelBackground) = when {
        score >= 80 -> Triple("Coincidencia alta", Teal, TealLight)
        score >= 50 -> Triple("Coincidencia media", Orange, Orange.copy(alpha = 0.16f))
        else -> Triple("Coincidencia baja", Red, Red.copy(alpha = 0.16f))
    }
    val isRejected = match.status?.uppercase() == "REJECTED"
    val isConfirmed = match.status?.uppercase() == "CONFIRMED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isRejected) 0.5f else 1f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Borders),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Teal, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "$score%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(levelBackground, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = levelLabel,
                            color = levelColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                    }
                }
                if (isRejected) {
                    Text(
                        text = "Descartada",
                        color = Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PetMatchColumn(
                    photoUrl = match.lostPetPhotoUrl,
                    photoBase64 = match.lostPetPhotoBase64,
                    petName = match.lostPetName,
                    statusLabel = "Perdida",
                    date = match.lostDate,
                    comuna = match.lostComuna,
                    statusColor = Red,
                    modifier = Modifier.weight(1f),
                )
                PetMatchColumn(
                    photoUrl = match.foundPetPhotoUrl,
                    photoBase64 = match.foundPetPhotoBase64,
                    petName = match.foundPetName,
                    statusLabel = "Encontrada",
                    date = match.foundDate,
                    comuna = match.foundComuna,
                    statusColor = Teal,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Teal),
                ) {
                    Text(text = "Ver detalles", color = Teal)
                }
                if (!isRejected) {
                    Button(
                        onClick = onContactClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    ) {
                        Text(text = "Contactar", color = Color.White)
                    }
                }
            }

            if (isConfirmed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ConfirmedBackground, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "¡Coincidencia confirmada!",
                        color = ConfirmedGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun PetMatchColumn(
    photoUrl: String?,
    photoBase64: String?,
    petName: String?,
    statusLabel: String,
    date: String?,
    comuna: String?,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            val imageBitmap = remember(photoBase64) { decodeBase64Image(photoBase64) }
            if (imageBitmap != null) {
                AsyncImage(
                    model = imageBitmap,
                    contentDescription = "Foto de mascota",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                )
            } else if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de mascota",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Text(
            text = petName?.let { "$it ($statusLabel)" } ?: statusLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = date ?: "Fecha no disponible",
            color = Gray,
            fontSize = 13.sp,
        )
        Text(
            text = comuna ?: "Comuna no disponible",
            color = Gray,
            fontSize = 13.sp,
        )
    }
}

private fun decodeBase64Image(base64Image: String?): ImageBitmap? {
    return try {
        if (base64Image.isNullOrBlank()) return null
        val decoded = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size) ?: return null
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}
