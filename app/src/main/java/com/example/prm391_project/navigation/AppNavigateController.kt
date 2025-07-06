package com.example.prm391_project

import TokenManager // Import TokenManager của bạn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation

// Import tất cả các màn hình cần thiết
import com.example.prm391_project.screen.user.MainScreenWithBottomNav
import com.example.prm391_project.screens.LoginScreen // Đảm bảo đúng import
import com.example.prm391_project.screen.auth.RegisterScreen
import com.example.prm391_project.screens.user.UpdateProfileScreen


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
    object ProductCart : Screen("product_cart_screen")
    object Favorites : Screen("favorites_screen")
    object Profile : Screen("profile_screen")
    object UpdateProfile : Screen("update_profile_screen") // THÊM DÒNG NÀY
}

@Composable
fun AppNavController(navController: NavHostController) { // Xóa isLoggedIn nếu bạn không truyền từ ngoài vào
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            // Nếu không có token, điều hướng đến màn hình Login
            // popupTo để clear stack nếu có màn hình khác và launchSingleTop
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else {
            // Nếu có token, điều hướng đến màn hình chính (MainAppGraph)
            navController.navigate(Screen.MainAppGraph.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    // Đặt startDestination là một route tạm thời (hoặc null) vì LaunchedEffect sẽ xử lý điều hướng ban đầu
    // Hoặc đặt là route mặc định nào đó mà bạn muốn hiển thị trong chốc lát
    NavHost(
        navController = navController,
        startDestination = "splash_screen_or_initial_check" // Route tạm thời cho lần đầu khởi tạo
    ) {
        // Đây là một route tạm thời để LaunchedEffect có thể hoạt động mà không bị lỗi
        // Bạn có thể đặt một màn hình Splash Screen ở đây
        composable("splash_screen_or_initial_check") {
            // Có thể hiển thị một ProgressBar hoặc Lottie animation nhỏ tại đây
            // Hoặc để trống nếu bạn muốn chuyển hướng nhanh
            // Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            //     Text("Đang kiểm tra đăng nhập...")
            // }
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.UpdateProfile.route) {
            UpdateProfileScreen(navController = navController)
        }
        // MainAppGraph chứa MainScreenWithBottomNav và các tab của nó
        navigation(
            startDestination = Screen.Home.route, // Route mặc định khi vào MainAppGraph
            route = Screen.MainAppGraph.route
        ) {
            composable(Screen.Home.route) {
                MainScreenWithBottomNav(outerNavController = navController)
            }
            // Không cần định nghĩa lại các composable cho "cart", "map", "chat", "setting" ở đây,
            // vì chúng đã được quản lý bên trong NavHost của MainScreenWithBottomNav.
            // Screen.ProductCart.route không cần thiết ở đây trừ khi bạn muốn điều hướng trực tiếp tới nó
            // từ bên ngoài MainAppGraph.
        }
    }
}