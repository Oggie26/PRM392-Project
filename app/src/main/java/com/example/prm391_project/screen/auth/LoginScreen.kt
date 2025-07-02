package com.example.prm391_project.screens

import TokenManager
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prm391_project.request.LoginRequest
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Import thêm Screen sealed class để sử dụng các route đã định nghĩa
import com.example.prm391_project.Screen // Đảm bảo import này đúng đường dẫn
import com.example.prm391_project.config.RetrofitClient

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8BBD0).copy(alpha = 0.2f), // Pastel pink
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(800)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo placeholder
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\n" +
                                "\uD83C\uDFE1",
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Happy Clothes",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Tên đăng nhập") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Username icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        AnimatedVisibility(visible = error.isNotEmpty()) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                loading = true
                                error = ""
                                coroutineScope.launch {
                                    try {
                                        val response: IResponse<LoginResponse> = RetrofitClient.authService.login(
                                            LoginRequest(username, password)
                                        )
                                        Log.d("Login", "Response status: ${response.code}, message: ${response.message}")
                                        if (response.code == 200) {
                                            // response.data lúc này sẽ chứa đối tượng LoginResponse (đã được ánh xạ từ "result" JSON)
                                            response.data?.let { loginResponseObject ->
                                                // Truy cập trực tiếp token từ loginResponseObject
                                                Log.d("token", loginResponseObject.token)
                                                tokenManager.saveToken(loginResponseObject.token)
                                            }
                                            val savedToken = tokenManager.getToken()
                                            Log.d("TokenDebug", "Saved and retrieved token: $savedToken")
                                            navController.navigate(com.example.prm391_project.Screen.MainAppGraph.route) {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                            }
                                        } else {
                                            error = response.message
                                        }
                                    } catch (e: HttpException) {
                                        error = when (e.code()) {
                                            401 -> "Sai tên đăng nhập hoặc mật khẩu."
                                            else -> "Lỗi server: ${e.message()}"
                                        }
                                    } catch (e: IOException) {
                                        Log.e("Login", "IOException: ${e.message}")
                                        error = "Không thể kết nối đến server. Vui lòng kiểm tra mạng."
                                    } catch (e: Exception) {
                                        error = "Đã xảy ra lỗi: ${e.message ?: "Không xác định"}"
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !loading && username.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    "Đăng nhập",
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Điều chỉnh dòng này để sử dụng Screen.Register.route
                        Text(
                            text = "Chưa có tài khoản? Đăng ký ngay",
                            modifier = Modifier
                                .clickable { navController.navigate(com.example.prm391_project.Screen.Register.route) } // <--- SỬA TẠI ĐÂY
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}