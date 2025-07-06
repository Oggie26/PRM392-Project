package com.example.prm391_project.screens.user

import TokenManager
import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.R
import com.example.prm391_project.Screen
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.request.UserProfileRequest
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.UserProfileResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    // State cho dữ liệu người dùng
    var userProfileState by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showUpdateSuccessDialog by remember { mutableStateOf(false) }

    // State cho các trường CÓ THỂ chỉnh sửa (hiển thị trong UI)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // State cho các trường KHÔNG hiển thị nhưng cần gửi trong request
    var avatar by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State cho việc tải lên hoặc lỗi khi cập nhật
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }

    // Thêm scroll state
    val scrollState = rememberScrollState()

    // Fetch user profile on launch
    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            error = "Không tìm thấy token. Vui lòng đăng nhập lại."
            isLoading = false
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }

        coroutineScope.launch {
            try {
                val authHeader = "Bearer $token"
                val response: IResponse<UserProfileResponse> = RetrofitClient.authService.getUserProfile(authHeader)

                if (response.code == 200 && response.data != null) {
                    userProfileState = response.data
                    // Initialize editable fields with current profile data
                    fullName = userProfileState?.fullName ?: ""
                    email = userProfileState?.email ?: ""
                    phone = userProfileState?.phone ?: ""
                    birthday = userProfileState?.birthday ?: ""
                    gender = userProfileState?.gender ?: ""
                    address = userProfileState?.address ?: ""
                    // Initialize non-editable fields for request
                    avatar = userProfileState?.avatar ?: "default_avatar"
                    username = userProfileState?.username ?: ""
                    password = "" // Mật khẩu mặc định là rỗng
                } else {
                    error = response.message ?: "Không thể lấy thông tin profile."
                }
            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("UpdateProfileScreen", "HTTP Exception: ${e.message()}", e)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("UpdateProfileScreen", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("UpdateProfileScreen", "General Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Date Picker Dialog
    var year: Int
    var month: Int
    var day: Int
    val calendar = Calendar.getInstance()
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = if (birthday.isNotEmpty()) LocalDate.parse(birthday, formatter) else LocalDate.now()
        year = date.year
        month = date.monthValue - 1 // Calendar.MONTH is 0-indexed
        day = date.dayOfMonth
    } catch (e: DateTimeParseException) {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            birthday = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
        }, year, month, day
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa Hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        // Avatar Section
                        Box(
                            modifier = Modifier
                                .size(120.dp)
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // Editable Fields
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Họ và tên") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Số điện thoại") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = birthday,
                                onValueChange = { birthday = it },
                                label = { Text("Ngày sinh (YYYY-MM-DD)") },
                                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = "Birthday") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() },
                                shape = RoundedCornerShape(12.dp)
                            )
                            GenderSelection(selectedGender = gender, onGenderSelected = { gender = it })
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Địa chỉ") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Update Button
                        Button(
                            onClick = {
                                val token = tokenManager.getToken()
                                val userId = userProfileState?.id

                                // Kiểm tra các trường bắt buộc (chỉ các trường hiển thị)
                                if (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
                                    Toast.makeText(context, "Lỗi: Không tìm thấy token hoặc ID người dùng.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (fullName.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng nhập họ và tên.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (email.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng nhập email.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (phone.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng nhập số điện thoại.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (birthday.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng chọn ngày sinh.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (address.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng nhập địa chỉ.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (gender.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng chọn giới tính.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isUpdating = true
                                updateError = null
                                val requestBody = UserProfileRequest(
                                    avatar = avatar,
                                    fullName = fullName,
                                    email = email,
                                    username = username,
                                    password = password,
                                    birthday = birthday,
                                    phone = phone,
                                    point = userProfileState?.point ?: 0,
                                    address = address,
                                    gender = gender,
                                    role = userProfileState?.role ?: "USER",
                                    status = userProfileState?.status ?: "ACTIVE"
                                )

                                coroutineScope.launch {
                                    try {
                                        val authHeader = "Bearer $token"
                                        Log.d("UpdateProfileScreen", "Auth Header: $authHeader")
                                        Log.d("UpdateProfileScreen", "User ID: $userId")
                                        Log.d("UpdateProfileScreen", requestBody.toString())

                                        val response: IResponse<UserProfileResponse> = RetrofitClient.authService.updateUserProfile(
                                            authHeader,
                                            userId,
                                            requestBody
                                        )

                                        if (response.code == 200 && response.data != null) {
                                            Toast.makeText(context, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                                            showUpdateSuccessDialog = true
                                            userProfileState = response.data
                                            fullName = userProfileState?.fullName ?: ""
                                            email = userProfileState?.email ?: ""
                                            phone = userProfileState?.phone ?: ""
                                            birthday = userProfileState?.birthday ?: ""
                                            gender = userProfileState?.gender ?: ""
                                            address = userProfileState?.address ?: ""
                                            avatar = userProfileState?.avatar ?: "default_avatar"
                                            username = userProfileState?.username ?: ""
                                            password = "" // Xóa mật khẩu sau khi gửi
                                            navController.popBackStack()
                                        } else {
                                            updateError = response.message ?: "Không thể cập nhật hồ sơ."
                                            Toast.makeText(context, "Lỗi cập nhật: ${updateError}", Toast.LENGTH_LONG).show()
                                            Log.e("UpdateProfileScreen", "Update failed: ${response.code} - ${response.message}")
                                        }
                                    } catch (e: HttpException) {
                                        updateError = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                                        Toast.makeText(context, "Lỗi HTTP: ${e.code()} - ${e.message()}", Toast.LENGTH_LONG).show()
                                        Log.e("UpdateProfileScreen", "HTTP Exception during update: ${e.message()}", e)
                                    } catch (e: IOException) {
                                        updateError = "Lỗi mạng: Không thể kết nối đến server."
                                        Toast.makeText(context, "Lỗi mạng: ${e.message}", Toast.LENGTH_LONG).show()
                                        Log.e("UpdateProfileScreen", "IO Exception during update: ${e.message}", e)
                                    } catch (e: Exception) {
                                        updateError = "Lỗi không xác định: ${e.message}"
                                        Toast.makeText(context, "Lỗi không xác định: ${e.message}", Toast.LENGTH_LONG).show()
                                        Log.e("UpdateProfileScreen", "General Exception during update: ${e.message}", e)
                                    } finally {
                                        isUpdating = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isUpdating
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "Cập nhật Hồ sơ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }

                        updateError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    )

    if (showUpdateSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateSuccessDialog = false },
            title = { Text("Thành công!") },
            text = { Text("Hồ sơ của bạn đã được cập nhật.") },
            confirmButton = {
                Button(onClick = {
                    showUpdateSuccessDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelection(selectedGender: String, onGenderSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("MALE", "FEMALE", "OTHER")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = { Text("Giới tính") },
            leadingIcon = { Icon(Icons.Default.Face, contentDescription = "Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genderOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onGenderSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = MenuDefaults.DropdownMenuItemContentPadding
                )
            }
        }
    }
}