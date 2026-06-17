package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sanosysalvosv2.viewmodel.NotificationListItem
import com.example.sanosysalvosv2.viewmodel.NotificacionesUiState
import com.example.sanosysalvosv2.viewmodel.NotificacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    viewModel: NotificacionesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeFilter = viewModel.activeFilter

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    val tabs = listOf("Todas", "Coincidencias", "Reportes", "Sistema")
    val selectedIndex = tabs.indexOf(activeFilter).takeIf { it >= 0 } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notificaciones",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D6A5F),
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp)
                            .clickable { navController.popBackStack() },
                        tint = Color(0xFF4A9B8E),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF4A9B8E),
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        color = Color(0xFF4A9B8E),
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.setFilter(tab) },
                        text = {
                            Text(
                                tab,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                            )
                        }
                    )
                }
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            when (val state = uiState) {
                is NotificacionesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4A9B8E))
                    }
                }

                is NotificacionesUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Color(0xFF4A9B8E).copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp),
                            )
                            Text(
                                "No hay notificaciones",
                                color = Color.Gray,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }

                is NotificacionesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color.Red.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp),
                            )
                            Text(state.message, color = Color.Red, fontSize = 14.sp)
                            OutlinedButton(
                                onClick = { viewModel.loadNotifications() },
                                border = BorderStroke(1.dp, Color(0xFF4A9B8E)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4A9B8E),
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is NotificacionesUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(state.notifications, key = { it.id }) { notification ->
                            NotificationListItemRow(notification = notification)
                            Divider(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationListItemRow(notification: NotificationListItem) {
    val (iconBg, icon) = when (notification.type) {
        "match" -> Pair(Color(0xFFFF6B6B).copy(alpha = 0.15f), Icons.Default.Warning)
        "report" -> Pair(Color(0xFF4A9B8E).copy(alpha = 0.15f), Icons.Default.Campaign)
        else -> Pair(Color(0xFF4A9B8E).copy(alpha = 0.15f), Icons.Default.CheckCircle)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (notification.type == "match") Color(0xFFE53935) else Color(0xFF4A9B8E),
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = notification.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2D2D2D),
            )
            Text(
                text = notification.subtitle,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (notification.timeAgo.isNotEmpty()) {
                Text(
                    text = notification.timeAgo,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                )
            }
        }
    }
}
