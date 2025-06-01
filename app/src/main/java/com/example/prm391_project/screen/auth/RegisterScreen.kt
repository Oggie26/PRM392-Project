//package com.example.prm391_project.screen.auth
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.prm391_project.request.RegisterRequest
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//@OptIn(ExperimentalMaterial3Api::class)
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun RegisterScreen(navController: NavController) {
//    var username by remember { mutableStateOf("") }
//    var fullName by remember { mutableStateOf("") }
//    var birthday by remember { mutableStateOf<LocalDate?>(null) }
//    var gender by remember { mutableStateOf("") }
//    var phone by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }
//    val snackbarHostState = remember { SnackbarHostState() }
//    var showPassword by remember { mutableStateOf(false) }
//    var showConfirmPassword by remember { mutableStateOf(false) }
//
//    // Gender options
//    val genderOptions = listOf("Male", "Female", "Other")
//    var genderExpanded by remember { mutableStateOf(false) }
//
//    // Date picker state
//    val context = LocalContext.current
//    val datePickerDialog = remember {
//        android.app.DatePickerDialog(
//            context,
//            { _, year, month, day ->
//                birthday = LocalDate.of(year, month + 1, day)
//            },
//            LocalDate.now().year,
//            LocalDate.now().monthValue - 1,
//            LocalDate.now().dayOfMonth
//        )
//    }
//
//    // Gradient background
//    val gradientBrush = Brush.verticalGradient(
//        colors = listOf(
//            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//            MaterialTheme.colorScheme.background
//        )
//    )
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(gradientBrush)
//                .padding(padding)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .verticalScroll(rememberScrollState())
//                    .padding(horizontal = 24.dp, vertical = 16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Header
//                AnimatedVisibility(
//                    visible = true,
//                    enter = fadeIn(animationSpec = tween(1000))
//                ) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Icon(
//                            imageVector = Icons.Default.AccountCircle,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(48.dp)
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "Tạo tài khoản mới",
//                            style = MaterialTheme.typography.headlineMedium.copy(
//                                fontSize = 28.sp,
//                                color = MaterialTheme.colorScheme.onBackground
//                            )
//                        )
//                        Text(
//                            text = "Điền thông tin để bắt đầu",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier.padding(top = 8.dp)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Form Card
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(16.dp)),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        // Username
//                        OutlinedTextField(
//                            value = username,
//                            onValueChange = { username = it },
//                            label = { Text("Tên đăng nhập") },
//                            leadingIcon = {
//                                Icon(Icons.Default.Person, contentDescription = null)
//                            },
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth(),
//                            supportingText = { if (username.isBlank()) Text("Vui lòng nhập tên đăng nhập") }
//                        )
//
//                        // Full Name
//                        OutlinedTextField(
//                            value = fullName,
//                            onValueChange = { fullName = it },
//                            label = { Text("Họ và tên") },
//                            leadingIcon = {
//                                Icon(Icons.Default.AccountCircle, contentDescription = null)
//                            },
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth(),
//                            supportingText = { if (fullName.isBlank()) Text("Vui lòng nhập họ và tên") }
//                        )
//
//                        // Birthday
//                        OutlinedTextField(
//                            value = birthday?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
//                            onValueChange = {},
//                            label = { Text("Ngày sinh (yyyy-MM-dd)") },
//                            leadingIcon = {
//                                Icon(Icons.Default.DateRange, contentDescription = null)
//                            },
//                            readOnly = true,
//                            singleLine = true,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable { datePickerDialog.show() },
//                            supportingText = { if (birthday == null) Text("Vui lòng chọn ngày sinh") }
//                        )
//
//                        // Gender Dropdown
//                        ExposedDropdownMenuBox(
//                            expanded = genderExpanded,
//                            onExpandedChange = { genderExpanded = !genderExpanded }
//                        ) {
//                            OutlinedTextField(
//                                value = gender,
//                                onValueChange = {},
//                                label = { Text("Giới tính") },
//                                leadingIcon = {
//                                    Icon(Icons.Default.Person, contentDescription = null)
//                                },
//                                readOnly = true,
//                                trailingIcon = {
//                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
//                                },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .menuAnchor(),
//                                supportingText = { if (gender.isBlank()) Text("Vui lòng chọn giới tính") }
//                            )
//                            ExposedDropdownMenu(
//                                expanded = genderExpanded,
//                                onDismissRequest = { genderExpanded = false }
//                            ) {
//                                genderOptions.forEach { option ->
//                                    DropdownMenuItem(
//                                        text = { Text(option) },
//                                        onClick = {
//                                            gender = option
//                                            genderExpanded = false
//                                        }
//                                    )
//                                }
//                            }
//                        }
//
//                        // Phone
//                        OutlinedTextField(
//                            value = phone,
//                            onValueChange = { phone = it },
//                            label = { Text("Số điện thoại") },
//                            leadingIcon = {
//                                Icon(Icons.Default.Phone, contentDescription = null)
//                            },
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth(),
//                            supportingText = { if (phone.isBlank()) Text("Vui lòng nhập số điện thoại") }
//                        )
//
//                        // Email
//                        OutlinedTextField(
//                            value = email,
//                            onValueChange = { email = it },
//                            label = { Text("Email") },
//                            leadingIcon = {
//                                Icon(Icons.Default.Email, contentDescription = null)
//                            },
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth(),
//                            supportingText = { if (email.isBlank()) Text("Vui lòng nhập email") }
//                        )
//
//                        // Password
//                        OutlinedTextField(
//                            value = password,
//                            onValueChange = { password = it },
//                            label = { Text("Mật khẩu") },
//                            leadingIcon = {
//                                Icon(Icons.Default.Lock, contentDescription = null)
//                            },
//                            trailingIcon = {
//                                IconButton(onClick = { showPassword = !showPassword }) {
//                                    Icon(
//                                        imageVector = if (showPassword) Icons.Default.Info else Icons.Default.Info,
//                                        contentDescription = if (showPassword) "Ẩn mật khẩu" else "Hiện mật khẩu"
//                                    )
//                                }
//                            },
//                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth(),
//                            supportingText = { if (password.isBlank()) Text("Vui lòng nhập mật khẩu") }
//                        )
//
//                        // Confirm Password
//                        OutlinedTextField(
//                            value = confirmPassword,
//                            onValueChange = { confirmPassword = it },
//                            label = { Text("Xác nhận mật khẩu") },
//                            leadingIcon = {
//                                Icon(Icons.Default.Lock, contentDescription = null)
//                            },
//                            trailingIcon = {
//                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
//                                    Icon(
//                                        imageVector = if (showConfirmPassword) Icons.Default.Info else Icons.Default.Info,
//                                        contentDescription = if (showConfirmPassword) "Ẩn mật khẩu" else "Hiện mật khẩu"
//                                    )
//                                }
//                            },
//                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                            singleLine = true,
//                            modifier = Modifier.fillMaxWidth(),
//                            supportingText = { if (confirmPassword.isBlank()) Text("Vui lòng xác nhận mật khẩu") }
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Register Button
//                Button(
//                    onClick = {
//                        isLoading = true
//                        // Validate fields
//                        if (username.isBlank() || fullName.isBlank() || birthday == null ||
//                            gender.isBlank() || phone.isBlank() || email.isBlank() ||
//                            password.isBlank() || confirmPassword.isBlank()
//                        ) {
//                            isLoading = false
//                            LaunchedEffect(Unit) {
//                                snackbarHostState.showSnackbar("Vui lòng điền đầy đủ thông tin")
//                            }
//                            return@Button
//                        }
//
//                        if (password != confirmPassword) {
//                            isLoading = false
//                            LaunchedEffect(Unit) {
//                                snackbarHostState.showSnackbar("Mật khẩu không khớp")
//                            }
//                            return@Button
//                        }
//
//                        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
//                            isLoading = false
//                            LaunchedEffect(Unit) {
//                                snackbarHostState.showSnackbar("Email không hợp lệ")
//                            }
//                            return@Button
//                        }
//
//                        if (!phone.matches(Regex("^\\+?[1-9]\\d{1,14}$"))) {
//                            isLoading = false
//                            LaunchedEffect(Unit) {
//                                snackbarHostState.showSnackbar("Số điện thoại không hợp lệ")
//                            }
//                            return@Button
//                        }
//
//                        try {
//                            val request = RegisterRequest(
//                                username = username,
//                                password = password,
//                                birthday = birthday!!,
//                                phone = phone,
//                                email = email,
//                                gender = gender,
//                                fullName = fullName
//                            )
//
//                            // TODO: Call API here with Retrofit
//                            println("Sending request: $request")
//
//                            isLoading = false
//                            LaunchedEffect(Unit) {
//                                snackbarHostState.showSnackbar("Đăng ký thành công!")
//                                navController.navigate("login") {
//                                    popUpTo("register") { inclusive = true }
//                                }
//                            }
//                        } catch (e: Exception) {
//                            isLoading = false
//                            LaunchedEffect(Unit) {
//                                snackbarHostState.showSnackbar("Lỗi: ${e.message}")
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    enabled = !isLoading
//                ) {
//                    if (isLoading) {
//                        CircularProgressIndicator(
//                            color = MaterialTheme.colorScheme.onPrimary,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    } else {
//                        Text("Đăng ký", fontSize = 18.sp)
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Đã có tài khoản? Đăng nhập tại đây",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier
//                        .clickable { navController.navigate("login") }
//                        .padding(8.dp),
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//    }
//}