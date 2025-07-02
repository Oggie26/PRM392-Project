package com.example.prm391_project.screen.auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prm391_project.request.RegisterRequest
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.RegisterResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Cần import Screen sealed class để truy cập các route đã định nghĩa
import com.example.prm391_project.Screen
import com.example.prm391_project.config.RetrofitClient

@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8BBD0).copy(alpha = 0.2f),
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
                    .verticalScroll(scrollState) // ✅ Cuộn được
                    .imePadding() // ✅ Tránh bàn phím
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Happy Clothes 👕",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Các field
                        InputField("Họ và tên", fullName, Icons.Default.Person) { fullName = it }
                        InputField("Tên đăng nhập", username, Icons.Default.AccountCircle) { username = it }
                        PasswordField("Mật khẩu", password) { password = it }
                        PasswordField("Xác nhận mật khẩu", confirmPassword) { confirmPassword = it }
                        InputField("Email", email, Icons.Default.Email, KeyboardType.Email) { email = it }
                        InputField("Số điện thoại", phone, Icons.Default.Phone, KeyboardType.Phone) { phone = it }

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
                                keyboardController?.hide()
                                loading = true
                                error = ""

                                coroutineScope.launch {
                                    if (password != confirmPassword) {
                                        error = "Mật khẩu và xác nhận mật khẩu không khớp."
                                        loading = false
                                        return@launch
                                    }

                                    try {
                                        val response: IResponse<RegisterResponse> =
                                            RetrofitClient.authService.register(
                                                RegisterRequest(
                                                    username = username,
                                                    password = password,
                                                    phone = phone,
                                                    email = email,
                                                    fullName = fullName
                                                )
                                            )
                                        if (response.code == 201) {
                                            loading = false
                                            // Điều hướng về màn hình đăng nhập sau khi đăng ký thành công
                                            // Sử dụng Screen.Login.route
                                            navController.navigate(Screen.Login.route) {
                                                // Optional: popUpTo để xóa màn hình đăng ký khỏi back stack
                                                // Nếu bạn muốn người dùng không quay lại màn hình đăng ký
                                                popUpTo(navController.graph.id) {
                                                    inclusive = true
                                                }
                                            }
                                        } else {
                                            error = response.message
                                        }
                                    } catch (e: HttpException) {
                                        error = when (e.code()) {
                                            400 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
                                            409 -> "Tên đăng nhập hoặc email đã tồn tại."
                                            else -> "Lỗi server: ${e.message()}"
                                        }
                                    } catch (e: IOException) {
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
                            enabled = listOf(fullName, username, password, confirmPassword, email, phone).all { it.isNotBlank() } && !loading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp) // ❌ Không shadow
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("Đăng ký", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Đã có tài khoản? Đăng nhập ngay",
                            modifier = Modifier
                                .clickable { navController.navigate(Screen.Login.route) } // <--- SỬA TẠI ĐÂY
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

@Composable
fun InputField(
    label: String,
    value: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}