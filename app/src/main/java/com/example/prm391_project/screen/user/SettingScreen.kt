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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.MainActivity
import com.example.prm391_project.R
import com.example.prm391_project.Screen
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.response.UserProfileResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    var userProfileState by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Lấy token một lần và sử dụng trong LaunchedEffect
    val token by remember { mutableStateOf(tokenManager.getToken()) }

    LaunchedEffect(token) {
        if (token.isNullOrEmpty()) {
            error = "Không tìm thấy token. Vui lòng đăng nhập lại."
            isLoading = false
            // Chỉ điều hướng nếu không ở trong MainAppGraph
            if (navController.currentDestination?.route != Screen.MainAppGraph.route) {
                Log.d("SettingsScreen", "No token, navigating to Login")
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                Log.d("SettingsScreen", "No token, but already in MainAppGraph, staying in Settings")
            }
            return@LaunchedEffect
        }

        coroutineScope.launch {
            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.authService.getUserProfile(authHeader)

                Log.d("SettingsScreen", "User Profile Response Code: ${response.code}")
                Log.d("SettingsScreen", "User Profile Response Message: ${response.message}")
                Log.d("SettingsScreen", "User Profile Response Data: ${response.data}")

                if (response.code == 200) {
                    userProfileState = response.data
                    Log.d("SettingsScreen", "User Full Name: ${userProfileState?.fullName}")
                } else {
                    error = response.message ?: "Không thể lấy thông tin profile."
                    // Nếu token không hợp lệ (401), xóa token và điều hướng về Login
                    if (response.code == 401) {
                        tokenManager.clearToken()
                        if (navController.currentDestination?.route != Screen.Login.route) {
                            Log.d("SettingsScreen", "Invalid token, navigating to Login")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("SettingsScreen", "HTTP Exception: ${e.message()}", e)
                if (e.code() == 401) {
                    tokenManager.clearToken()
                    if (navController.currentDestination?.route != Screen.Login.route) {
                        Log.d("SettingsScreen", "HTTP 401, navigating to Login")
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("SettingsScreen", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("SettingsScreen", "General Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Thông tin",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    lineHeight = 28.sp
                )
                Text(
                    text = "Cá nhân",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.voianimation))
                            val progress by animateLottieCompositionAsState(
                                composition = composition,
                                iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever
                            )
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Lỗi: $error",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    userProfileState != null -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = userProfileState?.avatar,
                                        contentDescription = "User Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(id = R.drawable.avatar_svgrepo_com)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = userProfileState?.fullName ?: "Không rõ tên",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = userProfileState?.email ?: "Không rõ email",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                InfoRow(label = "Điện thoại", value = userProfileState?.phone ?: "N/A", icon = Icons.Default.Phone)
                                InfoRow(label = "Ngày sinh", value = userProfileState?.birthday ?: "N/A", icon = Icons.Default.Cake)
                                InfoRow(label = "Giới tính", value = userProfileState?.gender ?: "N/A", icon = Icons.Default.Face)
                                InfoRow(label = "Địa chỉ", value = userProfileState?.address ?: "N/A", icon = Icons.Default.LocationOn)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        text = "Chỉnh sửa hồ sơ",
                        onClick = { navController.navigate(Screen.UpdateProfile.route) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        tokenManager.clearToken()
                        MainActivity.restartApp(context)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4444)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng xuất",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
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
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
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

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.DarkGray,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}