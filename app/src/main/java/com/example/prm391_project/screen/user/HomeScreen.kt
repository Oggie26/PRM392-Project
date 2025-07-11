@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prm391_project.R
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.response.ProductDetailResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import androidx.compose.material3.TextFieldDefaults


@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductDetailResponse>>(emptyList()) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }


//    val token by remember { mutableStateOf(tokenManager.getToken()) }
    val token = remember { mutableStateOf(tokenManager.getToken()) }

    val fetchProducts = {
        coroutineScope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitClient.productService.getProducts()
                if (response.code == 200 && response.data != null) {
                    products = response.data
                } else {
                    error = response.message ?: "Đã xảy ra lỗi khi tải sản phẩm."
                }
            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    Log.d("HomeScreen", "Token: ${token.value}")


    val fetchUserProfile: (String) -> Unit = { token ->
        coroutineScope.launch {
            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.authService.getUserProfile(authHeader)

                if (response.code == 200 && response.data != null) {
                    avatarUrl = response.data.avatar
                    fullName = response.data.fullName
                    Log.d("HomeScreen", "Avatar URL: $avatarUrl")
                } else {
                    Log.e("HomeScreen", "Lỗi lấy user profile: ${response.message}")
                }
            } catch (e: HttpException) {
                Log.e("HomeScreen", "HTTP error: ${e.code()} - ${e.message()}")
            } catch (e: IOException) {
                Log.e("HomeScreen", "Lỗi mạng: ${e.message}")
            } catch (e: Exception) {
                Log.e("HomeScreen", "Lỗi không xác định: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        token.value = tokenManager.getToken()
        Log.d("HomeScreen", "Token: ${token.value}")

        if (!token.value.isNullOrEmpty()) {
            fetchUserProfile(token.value!!)
        } else {
            Log.e("HomeScreen", "Token null => không gọi fetchUserProfile")
        }

        fetchProducts()
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(
//                    top = 0.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Hi, ${fullName ?: "User"}!", fontSize = 14.sp, color = Color.Gray)
                    Text("Welcome to Icot!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                if (!avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        error = painterResource(id = R.drawable.avatar_svgrepo_com), // fallback ảnh lỗi
                        fallback = painterResource(id = R.drawable.avatar_svgrepo_com) // fallback nếu null
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_svgrepo_com),
                        contentDescription = "Default Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Đảm bảo file tồn tại
                    contentDescription = "Search",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

//            // Filter row
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                listOf("All", "Nike", "Adidas", "Converse").forEachIndexed { index, label ->
//                    val isSelected = index == 0
//                    Text(
//                        text = label,
//                        color = if (isSelected) Color.White else Color.Black,
//                        modifier = Modifier
//                            .background(
//                                if (isSelected) Color.Black else Color(0xFFEFEFEF),
//                                shape = RoundedCornerShape(20.dp)
//                            )
//                            .padding(horizontal = 16.dp, vertical = 8.dp)
//                    )
//                }
//            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text("Lỗi: $error", color = Color.Red)
            } else {
                val rows = products.chunked(2)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { product ->
                                ProductItem(product, modifier = Modifier.weight(1f))
                            }
                            if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: ProductDetailResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(250.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = product.imageThumbnail,
                contentDescription = product.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = product.productName,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = "${product.price}₫",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
