package com.example.prm391_project

import TokenManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.prm391_project.screen.user.MainScreenWithBottomNav
import com.example.prm391_project.screens.LoginScreen
import com.example.prm391_project.screen.auth.RegisterScreen
import com.example.prm391_project.screens.user.UpdateProfileScreen
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Login : Screen("login_route")
    object Register : Screen("register_route")
    object MainAppGraph : Screen("main_app_graph")
    object Home : Screen("home_screen")
    object ProductCart : Screen("product_cart_screen")
    object Favorites : Screen("favorites_screen")
    object Profile : Screen("profile_screen")
    object UpdateProfile : Screen("update_profile_screen")
}

@Composable
fun AppNavController(navController: NavHostController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    // State để theo dõi trạng thái đăng nhập
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    // Kiểm tra token ban đầu
    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        Log.d("AppNavController", "Initial token check: $token")
        isLoggedIn = !token.isNullOrEmpty()
    }

    // Khi isLoggedIn thay đổi, điều hướng tương ứng
    LaunchedEffect(isLoggedIn) {
        // Đợi một chút để đảm bảo token được kiểm tra
        delay(100)
        Log.d("AppNavController", "isLoggedIn: $isLoggedIn, current route: ${navController.currentDestination?.route}")

        when (isLoggedIn) {
            true -> {
                // Có token, chỉ điều hướng nếu chưa ở MainAppGraph
                if (navController.currentDestination?.route != Screen.MainAppGraph.route) {
                    navController.navigate(Screen.MainAppGraph.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    Log.d("AppNavController", "Navigated to MainAppGraph")
                }
            }
            false -> {
                // Không có token, chỉ điều hướng nếu chưa ở Login
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    Log.d("AppNavController", "Navigated to Login")
                }
            }
            null -> {
                // Đang loading, không làm gì
                Log.d("AppNavController", "Waiting for token check")
            }
        }
    }

    // Xác định startDestination dựa trên trạng thái ban đầu
    val startDestination = Screen.Login.route // Luôn bắt đầu từ Login để đảm bảo kiểm tra token

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.UpdateProfile.route) {
            UpdateProfileScreen(navController = navController)
        }

        navigation(
            startDestination = Screen.Home.route,
            route = Screen.MainAppGraph.route
        ) {
            composable(Screen.Home.route) {
                MainScreenWithBottomNav(outerNavController = navController)
            }
        }
    }
}