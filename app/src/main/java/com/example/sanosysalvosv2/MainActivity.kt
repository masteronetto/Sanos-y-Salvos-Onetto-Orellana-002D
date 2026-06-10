package com.example.sanosysalvosv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.ui.screens.AdminCoincidenciasScreen
import com.example.sanosysalvosv2.ui.screens.AdminConfiguracionScreen
import com.example.sanosysalvosv2.ui.screens.AdminDashboardScreen
import com.example.sanosysalvosv2.ui.screens.AdminReporteDetailScreen
import com.example.sanosysalvosv2.ui.screens.AdminEntidadesScreen
import com.example.sanosysalvosv2.ui.screens.AdminEstadisticaScreen
import com.example.sanosysalvosv2.ui.screens.AdminMascotasScreen
import com.example.sanosysalvosv2.ui.screens.AdminReportesScreen
import com.example.sanosysalvosv2.ui.screens.AdminUsuariosScreen
import com.example.sanosysalvosv2.ui.screens.AddPetScreen
import com.example.sanosysalvosv2.ui.screens.CoincidenciasScreen
import com.example.sanosysalvosv2.ui.screens.NotificacionesScreen
import com.example.sanosysalvosv2.ui.screens.InicioScreen
import com.example.sanosysalvosv2.ui.screens.LoginScreen
import com.example.sanosysalvosv2.ui.screens.MapsScreen
import com.example.sanosysalvosv2.ui.screens.MascotasScreen
import com.example.sanosysalvosv2.ui.screens.PetDetailScreen
import com.example.sanosysalvosv2.ui.screens.PerfilScreen
import com.example.sanosysalvosv2.ui.screens.ReporteDetailScreen
import com.example.sanosysalvosv2.ui.screens.ReportesScreen
import com.example.sanosysalvosv2.ui.screens.ReportPetScreen
import com.example.sanosysalvosv2.ui.screens.RegisterScreen
import com.example.sanosysalvosv2.ui.theme.SanosYSalvosV2Theme
import com.example.sanosysalvosv2.viewmodel.AdminViewModel
import com.example.sanosysalvosv2.viewmodel.AuthViewModel
import com.example.sanosysalvosv2.viewmodel.MapsViewModel
import com.example.sanosysalvosv2.viewmodel.PetViewModel
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val mapViewModel: MapsViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private val petViewModel: PetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().osmdroidTileCache = cacheDir
        enableEdgeToEdge()
        setContent {
            SanosYSalvosV2Theme {
                Surface(modifier = Modifier) {
                    AppNav(
                        authViewModel = authViewModel,
                        mapViewModel = mapViewModel,
                        adminViewModel = adminViewModel,
                        petViewModel = petViewModel,
                    )
                }
            }
        }
    }
}

@Composable
fun AppNav(
    authViewModel: AuthViewModel,
    mapViewModel: MapsViewModel,
    adminViewModel: AdminViewModel,
    petViewModel: PetViewModel,
) {
    val navController = rememberNavController()
    val startDestination = if (authViewModel.isLoggedIn) {
        if (authViewModel.userRole == "ADMIN") "admin_dashboard" else "home" // TODO: add dedicated COLLABORATOR screen when feature is defined.
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("home") {
            UserTabsScaffold(
                authViewModel = authViewModel,
                mapViewModel = mapViewModel,
                petViewModel = petViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
            )
        }
        composable("admin_dashboard") {
            AdminTabsScaffold(
                adminViewModel = adminViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                },
            )
        }
    }
}

private data class AdminNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val adminBottomNavItems = listOf(
    AdminNavItem(route = "admin_dashboard", label = "Dashboard", icon = Icons.Filled.Home),
    AdminNavItem(route = "admin_usuarios", label = "Usuarios", icon = Icons.Filled.Person),
    AdminNavItem(route = "admin_mascotas", label = "Mascotas", icon = Icons.Filled.Pets),
    AdminNavItem(route = "admin_reportes", label = "Reportes", icon = Icons.Filled.Description),
    AdminNavItem(route = "admin_coincidencias", label = "Coincidencias", icon = Icons.Filled.TaskAlt),
    AdminNavItem(route = "admin_entidades", label = "Entidades", icon = Icons.Filled.Apartment),
    AdminNavItem(route = "admin_estadistica", label = "Estadistica", icon = Icons.Filled.BarChart),
    AdminNavItem(route = "admin_configuracion", label = "Configuración", icon = Icons.Filled.Settings),
)

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val userBottomNavItems = listOf(
    BottomNavItem(route = "inicio", label = "Inicio", icon = Icons.Filled.Home),
    BottomNavItem(route = "mascotas", label = "Mascotas", icon = Icons.Filled.Pets),
    BottomNavItem(route = "reportes", label = "Reportes", icon = Icons.Filled.Description),
    BottomNavItem(route = "mapas", label = "Mapas", icon = Icons.Filled.LocationOn),
    BottomNavItem(route = "perfil", label = "Perfil", icon = Icons.Filled.Person),
)

@Composable
private fun UserTabsScaffold(
    authViewModel: AuthViewModel,
    mapViewModel: MapsViewModel,
    petViewModel: PetViewModel,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val sessionStore = remember(context) { SessionStore(context.applicationContext) }
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = userBottomNavItems,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    tabNavController.navigate(route) {
                        popUpTo(tabNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = "inicio",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("inicio") {
                InicioScreen(
                    userName = "Camila",
                    onNavigateToNotifications = { tabNavController.navigate("notificaciones") },
                    onNavigateToAllNotifications = { tabNavController.navigate("notificaciones") },
                )
            }
            composable("mascotas") {
                MascotasScreen(
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToAddPet = { tabNavController.navigate("add_pet") },
                    onNavigateToPetDetail = { petId -> tabNavController.navigate("pet_detail/$petId") },
                )
            }
            composable("reportes") {
                ReportesScreen(
                    onNavigateToNewReport = { tabNavController.navigate("report_pet") },
                    onNavigateToReporteDetail = { reportId -> tabNavController.navigate("reporte_detail/$reportId") },
                )
            }
            composable("mapas") { MapasScreen(mapViewModel = mapViewModel) }
            composable("perfil") {
                PerfilScreen(
                    authViewModel = authViewModel,
                    sessionStore = sessionStore,
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToPersonalInfo = { tabNavController.navigate("personal_info") },
                    onNavigateToNotifications = { tabNavController.navigate("notificaciones") },
                    onNavigateToHelp = { tabNavController.navigate("ayuda") },
                    onNavigateToLogin = onLogout,
                )
            }
            composable("notificaciones") {
                NotificacionesScreen(
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("add_pet") { AddPetScreen() }
            composable("pet_detail/{petId}") { backStackEntry ->
                PetDetailScreen(
                    petId = backStackEntry.arguments?.getString("petId").orEmpty(),
                )
            }
            composable("report_pet") {
                ReportPetScreen(
                    petViewModel = petViewModel,
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("reporte_detail/{reportId}") { backStackEntry ->
                ReporteDetailScreen(
                    reportId = backStackEntry.arguments?.getString("reportId").orEmpty(),
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("coincidencias") {
                CoincidenciasScreen(
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("personal_info") { ScreenPlaceholder(title = "Información personal") }
            composable("ayuda") { ScreenPlaceholder(title = "Ayuda y soporte") }
        }
    }
}

@Composable
private fun AdminTabsScaffold(
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit,
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute == null || !currentRoute.startsWith("admin_reporte_detail")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AdminBottomNavBar(
                    items = adminBottomNavItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        tabNavController.navigate(route) {
                            popUpTo(tabNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = "admin_dashboard",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    onLogout = onLogout,
                )
            }
            composable("admin_usuarios") {
                AdminUsuariosScreen(
                    adminViewModel = adminViewModel,
                    onLogout = onLogout,
                )
            }
            composable("admin_mascotas") {
                AdminMascotasScreen(onLogout = onLogout)
            }
            composable("admin_reportes") {
                AdminReportesScreen(
                    onNavigateToReporteDetail = { reportId ->
                        tabNavController.navigate("admin_reporte_detail/$reportId")
                    },
                    onLogout = onLogout,
                )
            }
            composable("admin_reporte_detail/{reportId}") { backStackEntry ->
                AdminReporteDetailScreen(
                    reportId = backStackEntry.arguments?.getString("reportId").orEmpty(),
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("admin_coincidencias") {
                AdminCoincidenciasScreen(onLogout = onLogout)
            }
            composable("admin_entidades") {
                AdminEntidadesScreen(onLogout = onLogout)
            }
            composable("admin_estadistica") {
                AdminEstadisticaScreen(onLogout = onLogout)
            }
            composable("admin_configuracion") {
                AdminConfiguracionScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
private fun AdminBottomNavBar(
    items: List<AdminNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
private fun MapasScreen(mapViewModel: MapsViewModel) {
    MapsScreen(viewModel = mapViewModel)
}

@Composable
private fun ScreenPlaceholder(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = title)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SanosYSalvosV2Theme {
        Surface {
            Text("Sanos y Salvos V2")
        }
    }
}