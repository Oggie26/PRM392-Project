package com.example.prm391_project.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prm391_project.request.LoginRequest
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceVariant) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The Happy Coffee",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
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
                        contentDescription = "Username icon"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        contentDescription = "Password icon"
                    )
                }
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
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
                                navController.navigate("home") {
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
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && username.isNotBlank() && password.isNotBlank()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Đăng nhập")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    googleLoading = true
                    error = ""
                    coroutineScope.launch {
                        try {
                            // TODO: Implement Google sign-in logic
                            error = "Google sign-in not implemented yet."
                        } catch (e: Exception) {
                            error = "Lỗi đăng nhập bằng Google: ${e.message}"
                        } finally {
                            googleLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !googleLoading && !loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                if (googleLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Đăng nhập bằng Google", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chưa có tài khoản? Đăng ký ngay",
                modifier = Modifier.clickable { navController.navigate("register") },
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}