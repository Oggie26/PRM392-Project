package com.example.prm391_project.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.prm391_project.screens.LoginScreen
import com.example.prm391_project.screens.user.SettingsScreen
import com.example.prm391_project.common.CartStateHolder
import com.example.prm391_project.config.RetrofitClient
import TokenManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.prm391_project.ui.checkout.CheckoutScreen

data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val hasBadge: Boolean = false
)

@Composable
fun CustomBottomNavigation(
    navController: NavController,
    currentRoute: String,
    cartItemCount: Int,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = "cart",
            selectedIcon = Icons.Filled.ShoppingCart,
            unselectedIcon = Icons.Outlined.ShoppingCart,
            label = "Cart",
            hasBadge = true
        ),
        BottomNavItem(
            route = "map",
            selectedIcon = Icons.Filled.Map,
            unselectedIcon = Icons.Outlined.Map,
            label = "Map"
        ),
        BottomNavItem(
            route = "chat",
            selectedIcon = Icons.Filled.ChatBubble,
            unselectedIcon = Icons.Outlined.ChatBubbleOutline,
            label = "Chat"
        ),
        BottomNavItem(
            route = "setting",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            label = "Setting"
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D2D2D)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomNavItemView(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        badgeCount = if (item.hasBadge) cartItemCount else 0
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Text(badgeCount.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBottomNav(outerNavController: NavController) {
    val bottomNavController = rememberNavController()
    val currentBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val startRouteForBottomNav = "home"

    val cartItemCount by CartStateHolder.cartItemCount.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        if (!token.isNullOrEmpty()) {
            coroutineScope.launch {
                try {
                    val authHeader = "Bearer $token"
                    val response = RetrofitClient.cartService.getCart(authHeader)
                    if (response.code == 200) {
                        val cartResult = response.data
                        cartResult?.let { result ->
                            val itemCount = result.items?.size ?: 0
                            CartStateHolder.updateCartItemCount(itemCount)
                            Log.d("MainScreen", "Initial cart item count fetched: $itemCount")
                        }
                    } else {
                        Log.e("MainScreen", "Failed to fetch initial cart count: ${response.message}")
                        CartStateHolder.updateCartItemCount(0)
                    }
                } catch (e: HttpException) {
                    Log.e("MainScreen", "HTTP Exception fetching initial cart: ${e.code()} - ${e.message()}")
                    CartStateHolder.updateCartItemCount(0)
                } catch (e: IOException) {
                    Log.e("MainScreen", "IO Exception fetching initial cart: ${e.message}")
                    CartStateHolder.updateCartItemCount(0)
                } catch (e: Exception) {
                    Log.e("MainScreen", "General Exception fetching initial cart: ${e.message}")
                    CartStateHolder.updateCartItemCount(0)
                }
            }
        } else {
            CartStateHolder.updateCartItemCount(0)
            Log.d("MainScreen", "No token found, initial cart count set to 0.")
        }
    }

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(
                navController = bottomNavController,
                currentRoute = currentBackStackEntry?.destination?.route ?: startRouteForBottomNav,
                cartItemCount = cartItemCount
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = startRouteForBottomNav
            ) {
                composable("home") {
//                    HomeScreen(navController = bottomNavController)
                    HomeScreen(navController = outerNavController)
                }
                composable("cart") {
                    ProductCartScreen(navController = outerNavController)
                }
                composable("map") {
//                    Column(
//                        modifier = Modifier.fillMaxSize(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
                        MapScreen(navController = bottomNavController)
//                    }
                }
                composable("chat") {
//                    Column(
//                        modifier = Modifier.fillMaxSize(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Text("Chat Screen", style = MaterialTheme.typography.headlineMedium)
//                    }
                    ChatScreen(navController = bottomNavController)
                }
                composable("setting") {
                    SettingsScreen(navController = outerNavController)
                }

            }
        }
    }
}