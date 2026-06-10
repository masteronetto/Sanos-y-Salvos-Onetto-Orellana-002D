package com.example.sanosysalvosv2.ui.screens

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.ui.theme.TextAccent
import com.example.sanosysalvosv2.ui.theme.TextSecondary
import com.example.sanosysalvosv2.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.nio.charset.StandardCharsets

private data class ProfileData(
    val name: String,
    val email: String,
    val phone: String,
    val city: String,
)

@Composable
fun PerfilScreen(
    authViewModel: AuthViewModel,
    sessionStore: SessionStore,
    onNavigateBack: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    var profile by remember {
        mutableStateOf(
            ProfileData(
                name = "Usuario",
                email = "sin-correo@petfind.app",
                phone = "+56 9 0000 0000",
                city = "Santiago",
            ),
        )
    }

    LaunchedEffect(Unit) {
        val token = sessionStore.tokenFlow.first().orEmpty()
        profile = profileFromToken(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() },
            )

            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(TextAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Avatar",
                    tint = TextAccent,
                    modifier = Modifier.size(52.dp),
                )
            }

            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = profile.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = profile.city,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            PerfilMenuRow(
                icon = Icons.Filled.Person,
                label = "Información personal",
                onClick = onNavigateToPersonalInfo,
            )
            PerfilMenuRow(
                icon = Icons.Filled.Notifications,
                label = "Notificaciones",
                onClick = onNavigateToNotifications,
            )
            PerfilMenuRow(
                icon = Icons.Filled.Help,
                label = "Ayuda y soporte",
                onClick = onNavigateToHelp,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            TextButton(
                onClick = {
                    authViewModel.logout()
                    onNavigateToLogin()
                },
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PerfilMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextAccent,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
        )
    }
}

private fun profileFromToken(token: String): ProfileData {
    if (token.isBlank()) {
        return ProfileData(
            name = "Usuario",
            email = "sin-correo@petfind.app",
            phone = "+56 9 0000 0000",
            city = "Santiago",
        )
    }

    return try {
        val parts = token.split(".")
        if (parts.size < 2) throw IllegalArgumentException("Token JWT inválido")

        val payloadBytes = Base64.decode(
            parts[1],
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        val payloadJson = JSONObject(String(payloadBytes, StandardCharsets.UTF_8))

        val name = payloadJson.optString("fullName").ifBlank {
            payloadJson.optString("name").ifBlank {
                payloadJson.optString("userName").ifBlank { "Usuario" }
            }
        }

        val email = payloadJson.optString("email").ifBlank { "sin-correo@petfind.app" }
        val phone = payloadJson.optString("phone").ifBlank { "+56 9 0000 0000" }
        val city = payloadJson.optString("city").ifBlank { "Santiago" }

        ProfileData(
            name = name,
            email = email,
            phone = phone,
            city = city,
        )
    } catch (_: Exception) {
        ProfileData(
            name = "Usuario",
            email = "sin-correo@petfind.app",
            phone = "+56 9 0000 0000",
            city = "Santiago",
        )
    }
}
