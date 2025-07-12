@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.style.TextOverflow
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.Screen
import java.text.DecimalFormat
import java.util.Locale

// Data class cho filter categories
data class FilterCategory(
    val id: String,
    val name: String,
    val count: Int = 0
)

// Function để format giá tiền
fun formatPrice(price: Double): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(price)
}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductDetailResponse>>(emptyList()) }
    var filteredProducts by remember { mutableStateOf<List<ProductDetailResponse>>(emptyList()) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val token = remember { mutableStateOf(tokenManager.getToken()) }

    // Function to filter products
    val filterProducts = {
        filteredProducts = when {
            searchText.isBlank() && selectedCategory == "All" -> products
            searchText.isBlank() -> products.filter { product ->
                product.productName.contains(selectedCategory, ignoreCase = true) ||
                        product.description?.contains(selectedCategory, ignoreCase = true) == true
            }
            selectedCategory == "All" -> products.filter { product ->
                product.productName.contains(searchText, ignoreCase = true) ||
                        product.description?.contains(searchText, ignoreCase = true) == true
            }
            else -> products.filter { product ->
                (product.productName.contains(searchText, ignoreCase = true) ||
                        product.description?.contains(searchText, ignoreCase = true) == true) &&
                        (product.productName.contains(selectedCategory, ignoreCase = true) ||
                                product.description?.contains(selectedCategory, ignoreCase = true) == true)
            }
        }
    }

    // Update filtered products when search text or category changes
    LaunchedEffect(searchText, selectedCategory, products) {
        filterProducts()
    }

    val fetchProducts = {
        coroutineScope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitClient.productService.getProducts()
                if (response.code == 200 && response.data != null) {
                    products = response.data
                    filteredProducts = response.data
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

    Scaffold(
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Xin chào, ${fullName ?: "Bạn"}!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Chào mừng đến Icot!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                if (!avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .shadow(2.dp, CircleShape),
                        error = painterResource(id = R.drawable.avatar_svgrepo_com),
                        fallback = painterResource(id = R.drawable.avatar_svgrepo_com)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_svgrepo_com),
                        contentDescription = "Default Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .shadow(2.dp, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Simple Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Tìm kiếm", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simple filter and result count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tìm thấy ${filteredProducts.size} sản phẩm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Products Grid
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.trackloading))
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
            } else if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = "Lỗi: $error",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = filteredProducts.chunked(2)
                    items(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { product ->
                                ProductItem(
                                    product = product,
                                    modifier = Modifier.weight(1f),
                                    onProductClick = {
                                        navController.navigate(Screen.ProductDetail.createRoute(product.id.toString()))
                                    }
                                )
                            }
                            if (row.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: ProductDetailResponse,
    modifier: Modifier = Modifier,
    onProductClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(250.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Product Image
            AsyncImage(
                model = product.imageThumbnail,
                contentDescription = product.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name - Made larger, supports multiple lines
            Text(
                text = product.productName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                color = Color.Black,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price and Cart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Formatted Price
                Text(
                    text = "${formatPrice(product.price)}₫",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                // Shopping Cart Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Black, CircleShape)
                        .clickable {
                            // Add to cart logic here
                            Log.d("ProductItem", "Added ${product.productName} to cart")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}