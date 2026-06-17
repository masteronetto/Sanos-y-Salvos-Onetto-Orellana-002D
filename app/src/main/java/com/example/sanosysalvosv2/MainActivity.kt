package com.example.sanosysalvosv2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import timber.log.Timber
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.sanosysalvosv2.ui.screens.AdminMatchDetailScreen
import com.example.sanosysalvosv2.ui.screens.AdminReporteDetailScreen
import com.example.sanosysalvosv2.ui.screens.AdminEntidadesScreen
import com.example.sanosysalvosv2.ui.screens.AdminEstadisticaScreen
import com.example.sanosysalvosv2.ui.screens.AdminMascotasScreen
import com.example.sanosysalvosv2.ui.screens.AdminEditEntidadScreen
import com.example.sanosysalvosv2.ui.screens.AdminEditPetScreen
import com.example.sanosysalvosv2.ui.screens.AdminReportesScreen
import com.example.sanosysalvosv2.ui.screens.AdminUsuariosScreen
import com.example.sanosysalvosv2.ui.screens.AddEditPetScreen
import com.example.sanosysalvosv2.ui.screens.CoincidenciasScreen
import com.example.sanosysalvosv2.ui.screens.MatchDetailScreen
import com.example.sanosysalvosv2.ui.screens.EditProfileScreen
import com.example.sanosysalvosv2.ui.screens.NotificacionesScreen
import com.example.sanosysalvosv2.ui.screens.InicioScreen
import com.example.sanosysalvosv2.ui.screens.LoginScreen
import com.example.sanosysalvosv2.ui.screens.MapsScreen
import com.example.sanosysalvosv2.ui.screens.MascotasScreen
import com.example.sanosysalvosv2.ui.screens.PetDetailScreen
import com.example.sanosysalvosv2.ui.screens.PerfilScreen
import com.example.sanosysalvosv2.ui.screens.EditReportScreen
import com.example.sanosysalvosv2.ui.screens.CreateReportScreen
import com.example.sanosysalvosv2.ui.screens.ReporteDetailScreen
import com.example.sanosysalvosv2.ui.screens.ReportesScreen
import com.example.sanosysalvosv2.ui.screens.RegisterScreen
import com.example.sanosysalvosv2.ui.theme.SanosYSalvosV2Theme
import com.example.sanosysalvosv2.viewmodel.AdminViewModel
import com.example.sanosysalvosv2.viewmodel.UserReportsViewModel
import com.example.sanosysalvosv2.viewmodel.AuthViewModel
import com.example.sanosysalvosv2.viewmodel.MapsViewModel
import com.example.sanosysalvosv2.viewmodel.PetViewModel
import com.example.sanosysalvosv2.viewmodel.PetsViewModel
import com.example.sanosysalvosv2.viewmodel.ProfileViewModel
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val mapViewModel: MapsViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private val petViewModel: PetViewModel by viewModels()
    private val petsViewModel: PetsViewModel by viewModels()
    private val userReportsViewModel: UserReportsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private var notificationType: String? = null
    private var notificationMatchId: String? = null
    private var notificationReportId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().osmdroidTileCache = cacheDir

        // Handle notification intent
        handleNotificationIntent(intent)
        // Listen for new intents (app already running)
        enableEdgeToEdge()
        setContent {
            SanosYSalvosV2Theme {
                Surface(modifier = Modifier) {
                    AppNav(
                        authViewModel = authViewModel,
                        mapViewModel = mapViewModel,
                        adminViewModel = adminViewModel,
                        petViewModel = petViewModel,
                        petsViewModel = petsViewModel,
                        userReportsViewModel = userReportsViewModel,
                        profileViewModel = profileViewModel,
                        notificationType = notificationType,
                        notificationMatchId = notificationMatchId,
                        notificationReportId = notificationReportId,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("📬 New intent received")
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val matchId = intent?.getStringExtra("match_id")
            ?: intent?.getStringExtra("matchId")
        val reportId = intent?.getStringExtra("report_id")
            ?: intent?.getStringExtra("reportId")

        if (!matchId.isNullOrEmpty()) {
            Timber.d("Handling match notification: $matchId")
            notificationMatchId = matchId
            notificationReportId = null
        } else if (!reportId.isNullOrEmpty()) {
            Timber.d("Handling report notification: $reportId")
            notificationReportId = reportId
            notificationMatchId = null
        } else {
            notificationMatchId = intent?.getStringExtra("match_id")
            notificationReportId = intent?.getStringExtra("report_id")
        }

        notificationType = intent?.getStringExtra("notification_type")
    }
}

@Composable
fun AppNav(
    authViewModel: AuthViewModel,
    mapViewModel: MapsViewModel,
    adminViewModel: AdminViewModel,
    petViewModel: PetViewModel,
    petsViewModel: PetsViewModel,
    userReportsViewModel: UserReportsViewModel,
    profileViewModel: ProfileViewModel,
    notificationType: String?,
    notificationMatchId: String?,
    notificationReportId: String?,
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
                petsViewModel = petsViewModel,
                userReportsViewModel = userReportsViewModel,
                profileViewModel = profileViewModel,
                notificationType = notificationType,
                notificationMatchId = notificationMatchId,
                notificationReportId = notificationReportId,
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
    AdminNavItem(route = "admin_dashboard", label = "Inicio", icon = Icons.Filled.Home),
    AdminNavItem(route = "admin_usuarios", label = "Usuarios", icon = Icons.Filled.Person),
    AdminNavItem(route = "admin_mascotas", label = "Mascotas", icon = Icons.Filled.Pets),
    AdminNavItem(route = "admin_reportes", label = "Reportes", icon = Icons.Filled.Description),
    AdminNavItem(route = "admin_coincidencias", label = "Coincid.", icon = Icons.Filled.TaskAlt),
    AdminNavItem(route = "admin_entidades", label = "Entidades", icon = Icons.Filled.Apartment),
    AdminNavItem(route = "admin_estadistica", label = "Stats", icon = Icons.Filled.BarChart),
    AdminNavItem(route = "admin_configuracion", label = "Config", icon = Icons.Filled.Settings),
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
    petsViewModel: PetsViewModel,
    userReportsViewModel: UserReportsViewModel,
    profileViewModel: ProfileViewModel,
    notificationType: String?,
    notificationMatchId: String?,
    notificationReportId: String?,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val sessionStore = remember(context) { SessionStore(context.applicationContext) }
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(notificationMatchId, notificationReportId) {
        when {
            !notificationMatchId.isNullOrEmpty() -> {
                Timber.d("Navigating to match: $notificationMatchId")
                tabNavController.navigate("match_detail/$notificationMatchId") {
                    launchSingleTop = true
                }
            }
            !notificationReportId.isNullOrEmpty() -> {
                Timber.d("Navigating to report: $notificationReportId")
                tabNavController.navigate("reporte_detail/$notificationReportId") {
                    launchSingleTop = true
                }
            }
        }
    }

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
                    onNavigateToNotifications = { tabNavController.navigate("notificaciones") },
                    onNavigateToAllNotifications = { tabNavController.navigate("notificaciones") },
                )
            }
            composable("mascotas") {
                MascotasScreen(
                    petsViewModel = petsViewModel,
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToAddPet = { tabNavController.navigate("add_pet") },
                    onNavigateToPetDetail = { petId -> tabNavController.navigate("pet_detail/$petId") },
                    onNavigateToEditPet = { petId -> tabNavController.navigate("edit_pet/$petId") },
                )
            }
            composable("reportes") {
                ReportesScreen(
                    reportViewModel = userReportsViewModel,
                    onNavigateToNewReport = { tabNavController.navigate("create_report") },
                    onNavigateToReporteDetail = { reportId -> tabNavController.navigate("reporte_detail/$reportId") },
                    onNavigateToEditReport = { reportId -> tabNavController.navigate("edit_report/$reportId") },
                )
            }
            composable("mapas") { MapasScreen(mapViewModel = mapViewModel) }
            composable("perfil") {
                PerfilScreen(
                    profileViewModel = profileViewModel,
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToPersonalInfo = { tabNavController.navigate("personal_info") },
                    onNavigateToNotifications = { tabNavController.navigate("notificaciones") },
                    onNavigateToHelp = { tabNavController.navigate("ayuda") },
                    onNavigateToLogin = onLogout,
                )
            }
            composable("personal_info") {
                EditProfileScreen(
                    profileViewModel = profileViewModel,
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("notificaciones") {
                NotificacionesScreen(
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("add_pet") {
                AddEditPetScreen(
                    petViewModel = petsViewModel,
                    petId = null,
                    initialPet = null,
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("edit_pet/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId").orEmpty()
                LaunchedEffect(petId) {
                    if (petId.isNotBlank()) {
                        petsViewModel.loadPetDetails(petId)
                    }
                }
                val selectedPet by petsViewModel.selectedPet.collectAsState()
                if (selectedPet == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    AddEditPetScreen(
                        petViewModel = petsViewModel,
                        petId = petId,
                        initialPet = selectedPet,
                        onNavigateBack = { tabNavController.popBackStack() },
                    )
                }
            }
            composable("pet_detail/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId").orEmpty()
                PetDetailScreen(
                    petsViewModel = petsViewModel,
                    petId = petId,
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToEditPet = { id -> tabNavController.navigate("edit_pet/$id") },
                    onNavigateToReportPet = { id -> tabNavController.navigate("create_report") },
                )
            }
            composable("create_report") {
                CreateReportScreen(
                    navController = tabNavController,
                )
            }
            composable("reporte_detail/{reportId}") { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId").orEmpty()
                ReporteDetailScreen(
                    reportViewModel = userReportsViewModel,
                    reportId = reportId,
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToEditReport = { id -> tabNavController.navigate("edit_report/$id") },
                )
            }
            composable("edit_report/{reportId}") { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId").orEmpty()
                EditReportScreen(
                    reportViewModel = userReportsViewModel,
                    reportId = reportId,
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("coincidencias") {
                CoincidenciasScreen(
                    onNavigateBack = { tabNavController.popBackStack() },
                    onNavigateToMatchDetail = { matchId -> tabNavController.navigate("match_detail/$matchId") },
                )
            }
            composable("match_detail/{matchId}") { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId").orEmpty()
                MatchDetailScreen(
                    matchId = matchId,
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
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
                    onLogout = onLogout,
                )
            }
            composable("admin_usuarios") {
                AdminUsuariosScreen(
                    onLogout = onLogout,
                )
            }
            composable("admin_mascotas") {
                AdminMascotasScreen(onLogout = onLogout, onNavigateToEditPet = { petId ->
                    if (petId == null) tabNavController.navigate("admin_edit_pet/null") else tabNavController.navigate("admin_edit_pet/$petId")
                })
            }
            composable("admin_edit_pet/{petId}") { backStackEntry ->
                val petIdArg = backStackEntry.arguments?.getString("petId")
                val petId = petIdArg?.takeIf { it != "null" }
                AdminEditPetScreen(petId = petId, onNavigateBack = { tabNavController.popBackStack() })
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
                AdminCoincidenciasScreen(
                    onLogout = onLogout,
                    onNavigateToMatchDetail = { matchId ->
                        tabNavController.navigate("admin_match_detail/$matchId")
                    },
                )
            }
            composable("admin_match_detail/{matchId}") { backStackEntry ->
                AdminMatchDetailScreen(
                    matchId = backStackEntry.arguments?.getString("matchId").orEmpty(),
                    onNavigateBack = { tabNavController.popBackStack() },
                )
            }
            composable("admin_entidades") {
                AdminEntidadesScreen(
                    onLogout = onLogout,
                    onNavigateToEditEntidad = { entidadId ->
                        val route = entidadId?.let { "admin_edit_entidad/$it" } ?: "admin_edit_entidad/null"
                        tabNavController.navigate(route)
                    },
                )
            }
            composable("admin_edit_entidad/{entidadId}") { backStackEntry ->
                val entidadIdArg = backStackEntry.arguments?.getString("entidadId")
                val entidadId = entidadIdArg?.takeIf { it != "null" }
                AdminEditEntidadScreen(entidadId = entidadId, onNavigateBack = { tabNavController.popBackStack() })
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
    val activeColor = Color(0xFF0F8A8A)
    val inactiveColor = Color(0xFF9E9E9E)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider(color = Color(0xFFE5E5E5), thickness = 1.dp)
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                tonalElevation = 0.dp,
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        label = {
                            Text(item.label, fontSize = 9.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor,
                            indicatorColor = activeColor,
                        ),
                    )
                }
            }
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