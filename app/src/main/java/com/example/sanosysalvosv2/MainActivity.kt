package com.example.sanosysalvosv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sanosysalvosv2.ui.screens.AdminDashboardScreen
import com.example.sanosysalvosv2.ui.screens.HomeScreen
import com.example.sanosysalvosv2.ui.screens.LoginScreen
import com.example.sanosysalvosv2.ui.screens.RegisterScreen
import com.example.sanosysalvosv2.ui.screens.ReportPetScreen
import com.example.sanosysalvosv2.ui.theme.SanosYSalvosV2Theme
import com.example.sanosysalvosv2.viewmodel.AdminViewModel
import com.example.sanosysalvosv2.viewmodel.AuthViewModel
import com.example.sanosysalvosv2.viewmodel.MatchViewModel
import com.example.sanosysalvosv2.viewmodel.MapsViewModel
import com.example.sanosysalvosv2.viewmodel.PetViewModel
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val mapViewModel: MapsViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private val petViewModel: PetViewModel by viewModels()
    private val matchViewModel: MatchViewModel by viewModels()

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
                        matchViewModel = matchViewModel,
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
    matchViewModel: MatchViewModel,
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
            HomeScreen(
                matchViewModel = matchViewModel,
                mapViewModel = mapViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToReportPet = {
                    navController.navigate("report_pet")
                },
            )
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(
                adminViewModel = adminViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                },
            )
        }
        composable("report_pet") {
            ReportPetScreen(
                petViewModel = petViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }
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