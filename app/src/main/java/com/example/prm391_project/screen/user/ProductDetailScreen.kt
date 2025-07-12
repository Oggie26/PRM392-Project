package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.R
import com.example.prm391_project.common.CartStateHolder
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.response.ProductDetailResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

data class ProductDetail(
    val id: String,
    val productName: String,
    val price: String,
    val imageThumnail: String,
    val image: String,
    val quantity: Int,
    val color: String,
    val sizes: String,
    val description: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    outerNavController: NavController,
    productId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isAddingToCart by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var cartError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<ProductDetailResponse?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var selectedSize by remember { mutableStateOf("") }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    // Logic gọi API chi tiết sản phẩm
    val fetchProductDetail: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            error = null

            try {
                val response = RetrofitClient.productService.getProductDetail(productId)
                Log.d("ProductDetailScreen", "Mã phản hồi API: ${response.code}")
                Log.d("ProductDetailScreen", "Thông điệp phản hồi API: ${response.message}")
                Log.d("ProductDetailScreen", "Dữ liệu phản hồi API: ${response.data}")

                if (response.code == 200 && response.data != null) {
                    product = response.data
                } else {
                    error = response.message ?: "Không tìm thấy sản phẩm."
                }
            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("ProductDetailScreen", "Lỗi HTTP: ${e.message()}", e)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("ProductDetailScreen", "Lỗi IO: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("ProductDetailScreen", "Lỗi tổng quát: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Logic thêm vào giỏ hàng
    val handleAddToCart: () -> Unit = {
        coroutineScope.launch {
            isAddingToCart = true
            cartError = null

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                cartError = "Bạn chưa đăng nhập. Vui lòng đăng nhập để thêm vào giỏ hàng."
                isAddingToCart = false
                CartStateHolder.updateCartItemCount(0)
                return@launch
            }

            if (selectedSize.isEmpty()) {
                cartError = "Vui lòng chọn kích thước trước khi thêm vào giỏ hàng."
                isAddingToCart = false
                return@launch
            }

            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.cartService.addToCart(
                    authHeader,
                    productId,
                    quantity,
                    selectedSize
                )

                Log.d("ProductDetailScreen", "Mã phản hồi API Cart: ${response.code}")
                Log.d("ProductDetailScreen", "Thông điệp phản hồi API Cart: ${response.message}")
                Log.d("ProductDetailScreen", "Dữ liệu phản hồi API Cart: ${response.data}")

                if (response.code == 200) {
                    // Thành công - reset form
                    cartError = null
                    successMessage = "Đã thêm sản phẩm vào giỏ hàng thành công!"
                    quantity = 1
                    selectedSize = ""
                    // Tự động ẩn thông báo sau 3 giây
                    kotlinx.coroutines.delay(3000)
                    successMessage = null
                } else {
                    cartError = response.message ?: "Thêm vào giỏ hàng thất bại."
                }

            } catch (e: HttpException) {
                cartError = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("ProductDetailScreen", "Lỗi HTTP: ${e.message()}", e)
            } catch (e: IOException) {
                cartError = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("ProductDetailScreen", "Lỗi IO: ${e.message}", e)
            } catch (e: Exception) {
                cartError = "Lỗi không xác định: ${e.message}"
                Log.e("ProductDetailScreen", "Lỗi tổng quát: ${e.message}", e)
            } finally {
                isAddingToCart = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchProductDetail()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (error!!.contains("502")) "Máy chủ tạm thời gặp sự cố, vui lòng thử lại sau." else "Lỗi: $error",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { fetchProductDetail() }) {
                    Text("Thử lại")
                }
            }
        }
    } else {
        val currentProduct by remember { derivedStateOf { product } }
        if (currentProduct != null) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Chi tiết sản phẩm", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { outerNavController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 0.dp),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    val images = remember(product) {
                                        listOf(product!!.imageThumbnail) + product!!.images.map { it.image }
                                    }
                                    val pagerState = rememberPagerState(pageCount = { images.size })
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize()
                                        ) { page ->
                                            Image(
                                                painter = rememberAsyncImagePainter(images[page]),
                                                contentDescription = "Ảnh sản phẩm ${page + 1}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(8.dp)
                                        ) {
                                            images.forEachIndexed { index, _ ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            color = Color.Gray.copy(alpha = if (index == pagerState.currentPage) 1f else 0.3f),
                                                            shape = CircleShape
                                                        )
                                                        .padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = product!!.productName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(product!!.price),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF706B6B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFFFFF), RoundedCornerShape(4.dp))
                                            .border(width = 1.dp, color = Color(0xFF000000), RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            text = product!!.category!!.categoryName,
                                            fontSize = 12.sp,
                                            color = Color(0xFF000000),
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("4.5", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("(15 Đánh giá)", color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Mô tả", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = product!!.description,
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Loại vải", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = product!!.fabric!!.fabricName,
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Màu", fontWeight = FontWeight.SemiBold)
                                    Row {
                                        listOf(Color.White).forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(color, CircleShape)
                                                    .border(1.dp, Color.Gray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Kích thước", fontWeight = FontWeight.SemiBold)
                                    Row {
                                        product!!.sizes.forEach { size ->
                                            OutlinedButton(
                                                onClick = { selectedSize = size.size },
                                                modifier = Modifier.padding(end = 4.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (selectedSize == size.size) Color(0xFFC4C3C3) else Color.Transparent
                                                )
                                            ) {
                                                Text(size.size)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Tăng / giảm số lượng
//                                    Text("Số lượng", fontWeight = FontWeight.SemiBold)
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Button(
//                                            onClick = { if (quantity > 1) quantity-- },
//                                            modifier = Modifier.size(36.dp),
//                                            shape = RoundedCornerShape(4.dp),
//                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
//                                            contentPadding = PaddingValues(0.dp)
//                                        ) {
//                                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                                        }
//
//                                        Text(
//                                            text = quantity.toString(),
//                                            fontSize = 20.sp,
//                                            fontWeight = FontWeight.Medium,
//                                            modifier = Modifier.padding(horizontal = 24.dp)
//                                        )
//
//                                        Button(
//                                            onClick = { quantity++ },
//                                            modifier = Modifier.size(36.dp),
//                                            shape = RoundedCornerShape(4.dp),
//                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
//                                            contentPadding = PaddingValues(0.dp)
//                                        ) {
//                                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                                        }
//                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp), // Chiều cao bạn có thể chỉnh tùy ý
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Button(
                                                    onClick = { if (quantity > 1) quantity-- },
                                                    modifier = Modifier.size(36.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Text(
                                                    text = quantity.toString(),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 24.dp)
                                                )

                                                Button(
                                                    onClick = { quantity++ },
                                                    modifier = Modifier.size(36.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                }

                                // Hiển thị thông báo thành công
                                if (successMessage != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .background(Color(0xFFE8F5E8), RoundedCornerShape(8.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = successMessage!!,
                                                color = Color(0xFF4CAF50),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Hiển thị lỗi cart nếu có
                                if (cartError != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = cartError!!,
                                                color = Color.Red,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                // Nút thêm vào giỏ hàng với loading state
                                Button(
                                    onClick = { handleAddToCart() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF050505)),
                                    enabled = !isAddingToCart
                                ) {
                                    if (isAddingToCart) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Đang thêm...", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("Thêm vào giỏ hàng", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
