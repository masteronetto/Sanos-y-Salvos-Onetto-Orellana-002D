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
import com.example.sanosysalvosv2.ui.theme.SanosYSalvosV2Theme
import com.example.sanosysalvosv2.viewmodel.AdminViewModel
import com.example.sanosysalvosv2.viewmodel.AuthViewModel
import com.example.sanosysalvosv2.viewmodel.MapViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanosYSalvosV2Theme {
                Surface(modifier = Modifier) {
                    AppNav(
                        authViewModel = authViewModel,
                        mapViewModel = mapViewModel,
                        adminViewModel = adminViewModel,
                    )
                }
            }
        }
    }
}

@Composable
fun AppNav(
    authViewModel: AuthViewModel,
    mapViewModel: MapViewModel,
    adminViewModel: AdminViewModel,
) {
    val navController = rememberNavController()
    val startDestination = if (authViewModel.isLoggedIn) {
        if (authViewModel.userRole == "ADMIN") "admin_dashboard" else "home"
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
                mapViewModel = mapViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
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