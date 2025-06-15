// AppNavController.kt
package com.example.prm391_project

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation // Import navigation function for nested graphs

// Import tất cả các màn hình cần thiết
import com.example.prm391_project.screen.user.MainScreenWithBottomNav
import com.example.prm391_project.screen.user.ProductCartScreen // Nếu bạn cần ProductCartScreen trực tiếp ở cấp cao nhất
import com.example.prm391_project.screens.LoginScreen
//import com.example.prm391_project.screens.Regis // Đảm bảo bạn có file RegisterScreen này
import com.example.prm391_project.screens.LoginScreen

// Define your navigation routes
sealed class Screen(val route: String) {
    // Authentication routes
    object Login : Screen("login_route")
    object Register : Screen("register_route")

    // Main application graph route (this graph contains the bottom navigation)
    object MainAppGraph : Screen("main_app_graph")

    // Routes within the MainAppGraph that are managed by MainScreenWithBottomNav
    // These routes are used internally by MainScreenWithBottomNav's NavHost
    // They are also used as `startDestination` for the MainAppGraph itself (e.g., Home)
    object Home : Screen("home_screen")
    object ProductCart : Screen("product_cart_screen") // Route for the cart screen
    // Add other routes for tabs like Favorites, Profile if needed here
    object Favorites : Screen("favorites_screen")
    object Profile : Screen("profile_screen")
}

@Composable
fun AppNavController(navController: NavHostController, isLoggedIn: Boolean) {
    NavHost(
        navController = navController,
        startDestination =  Screen.MainAppGraph.route
    ) {
        composable(Screen.Login.route) {

            LoginScreen(navController)
        }
//        composable(Screen.Register.route) {
//
//            RegisterScreen(navController)
//        }


        navigation(
            startDestination = Screen.Home.route,
            route = Screen.MainAppGraph.route
        ) {

            composable(Screen.Home.route) {

                MainScreenWithBottomNav(navController)
            }

        }
    }
}