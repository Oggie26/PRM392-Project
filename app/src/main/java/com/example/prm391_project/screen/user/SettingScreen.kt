package com.example.prm391_project.screens.user

import TokenManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Cần import tất cả cho remember, LaunchedEffect, mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// import androidx.navigation.NavGraph.Companion.findStartDestination // <-- LOẠI BỎ IMPORT NÀY
import com.example.prm391_project.R // Make sure this import is correct
import com.example.prm391_project.Screen // Import your Screen sealed class
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.response.UserProfileResponse // <-- IMPORT UserProfileResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) } // Sử dụng applicationContext
    val coroutineScope = rememberCoroutineScope()

    var userProfileState by remember { mutableStateOf<UserProfileResponse?>(null) } // Đổi tên để tránh nhầm lẫn với data class UserProfile
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            error = "Không tìm thấy token. Vui lòng đăng nhập lại."
            isLoading = false
            // Điều hướng về LoginScreen nếu không có token
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true } // Xóa toàn bộ back stack
            }
            return@LaunchedEffect
        }

        coroutineScope.launch {
            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.authService.getUserProfile(authHeader)

                Log.d("SettingScreen", "User Profile Response Code: ${response.code}")
                Log.d("SettingScreen", "User Profile Response Message: ${response.message}")

                if (response.code == 200) {
                    // response.data (là UserProfileResponse) giờ đã ánh xạ từ "result" JSON
                    userProfileState = response.data
                    Log.d("SettingScreen", "User Full Name: ${userProfileState?.fullName}")
                } else {
                    error = response.message ?: "Không thể lấy thông tin profile."
                }
            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("SettingScreen", "HTTP Exception: ${e.message()}", e)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("SettingScreen", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("SettingScreen", "General Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Đang tải...", fontSize = 16.sp, color = Color.Gray)
                    } else if (error != null) {
                        Text("Lỗi: $error", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                    } else if (userProfileState != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User Avatar
                            // Sử dụng Coil hoặc Glide để tải ảnh từ userProfileState?.avatar
                            // Hiện tại dùng placeholder local
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Hoặc dùng Coil: rememberImagePainter(userProfileState?.avatarUrl)
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = userProfileState?.fullName ?: "Không rõ tên",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = userProfileState?.email ?: "Không rõ email",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        IconButton(onClick = { /* Handle edit profile click */ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Edit Profile", tint = Color.Gray)
                        }
                    } else {
                        // Trường hợp không có dữ liệu và không có lỗi
                        Text("Không có thông tin profile", fontSize = 16.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Các mục cài đặt khác nếu có, có thể sử dụng SettingsSection và SettingsItem
            // Ví dụ:
            // SettingsSection(title = "General Settings") {
            //     SettingsItem(icon = Icons.Default.Notifications, text = "Notifications", onClick = { /* ... */ })
            //     SettingsItem(icon = Icons.Default.Language, text = "Language", onClick = { /* ... */ })
            // }

            // Spacer to push the logout button to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    // 1. Clear the token from SharedPreferences
                    tokenManager.clearToken()
                    // 2. Navigate back to the LoginScreen
                    navController.navigate(Screen.Login.route) {
                        // Sửa lỗi Unresolved reference: findStartDestination
                        // Pop up to the LoginScreen and make it inclusive (xóa màn hình Login khỏi back stack)
                        popUpTo(Screen.Login.route) { // <-- Dùng route của LoginScreen
                            inclusive = true
                        }
                        // Hoặc xóa toàn bộ back stack để chắc chắn không còn màn hình nào trước Login
                        // popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Các Composable phụ trợ bạn đã cung cấp, không cần sửa đổi nếu chúng hoạt động độc lập
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.5f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go to",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}