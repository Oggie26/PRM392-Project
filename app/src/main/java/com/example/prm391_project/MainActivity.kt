// File: app/src/main/java/com/example/prm391_project/MainActivity.kt
package com.example.prm391_project

import TokenManager
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.ui.theme.PRM391_ProjectTheme
import com.example.prm391_project.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController
    private var pendingCartNavigation = false // Flag để theo dõi navigation đang chờ

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "POST_NOTIFICATIONS permission granted.")
                checkCartAndShowNotification()
            } else {
                Log.w("MainActivity", "POST_NOTIFICATIONS permission denied. Cannot show cart notification.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        // Kiểm tra permission và hiển thị notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkCartAndShowNotification()
            }
        } else {
            checkCartAndShowNotification()
        }

        setContent {
            PRM391_ProjectTheme {
                navController = rememberNavController()

                // State để theo dõi navigation đến cart
                var shouldNavigateToCart by remember { mutableStateOf(false) }

                // Xử lý navigation từ notification
                LaunchedEffect(navController) {
                    handleInitialIntent(intent)
                    if (pendingCartNavigation) {
                        shouldNavigateToCart = true
                        pendingCartNavigation = false
                    }
                }

                // Xử lý navigation đến cart khi cần thiết
                LaunchedEffect(shouldNavigateToCart) {
                    if (shouldNavigateToCart) {
                        // Đợi một chút để đảm bảo navigation graph đã được khởi tạo
                        delay(500)
                        navigateToCart()
                        shouldNavigateToCart = false
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavController(navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Xử lý intent khi activity resume từ background
        handleNewIntent(intent)
    }

    private fun handleInitialIntent(intent: Intent?) {
        intent?.let {
            val navigateToCart = it.getBooleanExtra("navigate_to_cart", false)
            if (navigateToCart) {
                pendingCartNavigation = true
                Log.d("MainActivity", "Initial intent detected: navigate to cart")
            }
        }
    }

    private fun handleNewIntent(intent: Intent?) {
        if (!::navController.isInitialized) {
            Log.w("MainActivity", "NavController not initialized yet. Deferring navigation.")
            pendingCartNavigation = true
            return
        }

        intent?.let {
            val navigateToCart = it.getBooleanExtra("navigate_to_cart", false)
            if (navigateToCart) {
                Log.d("MainActivity", "New intent detected: navigate to cart")
                navigateToCart()
                // Xóa flag để tránh navigation lại
                it.removeExtra("navigate_to_cart")
                it.removeExtra("navigate_to_route")
            }
        }
    }

    private fun navigateToCart() {
        try {
            // Đầu tiên, đảm bảo chúng ta ở trong MainAppGraph
            if (navController.currentDestination?.route != Screen.MainAppGraph.route) {
                navController.navigate(Screen.MainAppGraph.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }

            // Sau đó navigate đến cart trong bottom navigation
            // Cần delay ngắn để đảm bảo MainAppGraph đã được load
            CoroutineScope(Dispatchers.Main).launch {
                delay(100) // Delay ngắn
                try {
                    // Sử dụng route của cart trong bottom navigation
                    navController.navigate("cart") {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    Log.d("MainActivity", "Successfully navigated to cart")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error navigating to cart: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in navigateToCart: ${e.message}")
        }
    }

    private fun checkCartAndShowNotification() {
        // Kiểm tra xem có thể hiển thị heads-up notification không
        if (!NotificationHelper.canShowHeadsUpNotification(this)) {
            Log.w("MainActivity", "Cannot show heads-up notification. Check notification settings.")
            // Có thể hiển thị dialog hướng dẫn user
            showNotificationSettingsDialog()
            return
        }

        val tokenManager = TokenManager(applicationContext)
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                Log.d("MainActivity", "No token found. Cannot check cart for notification.")
                return@launch
            }

            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.cartService.getCart(authHeader)

                if (response.code == 200) {
                    val cartResult = response.data
                    val totalQuantity = cartResult?.items?.size ?: 0

                    withContext(Dispatchers.Main) {
                        if (totalQuantity > 0) {
                            // Delay một chút để đảm bảo app đã sẵn sàng
                            delay(1000)
                            NotificationHelper.showCartNotification(applicationContext, totalQuantity)
                            Log.d("MainActivity", "Cart has $totalQuantity items. Showing heads-up notification.")
                        } else {
                            Log.d("MainActivity", "Cart is empty. No notification needed.")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch cart: ${response.code} - ${response.message}")
                }
            } catch (e: HttpException) {
                Log.e("MainActivity", "HTTP Exception: ${e.message()}", e)
            } catch (e: IOException) {
                Log.e("MainActivity", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("MainActivity", "General Exception: ${e.message}", e)
            }
        }
    }

    // Thêm method để hiển thị dialog hướng dẫn notification settings
    private fun showNotificationSettingsDialog() {
        // Có thể implement AlertDialog để hướng dẫn user
        // hoặc navigate đến notification settings
        Log.d("MainActivity", "Should show notification settings dialog")

        // Tạo intent để mở notification settings
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
            }
        } else {
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
            }
        }

        // Có thể hiển thị dialog trước khi mở settings
        // startActivity(intent)
    }

    // Thêm method để test notification (có thể gọi từ button trong UI)
    fun testNotification() {
        NotificationHelper.showTestNotification(this)
    }
}