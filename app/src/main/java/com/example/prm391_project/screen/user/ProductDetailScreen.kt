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

@Composable
fun ProductDetailScreen(
    outerNavController: NavController,
    productId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
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
            isLoading = true
            error = null

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                error = "Bạn chưa đăng nhập. Vui lòng đăng nhập để xem giỏ hàng."
                isLoading = false
                CartStateHolder.updateCartItemCount(0)
                return@launch
            }

            if (selectedSize.isEmpty()) {
                error = "Vui lòng chọn kích thước trước."
                isLoading = false
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
//                    CartStateHolder.updateCartItemCount(response.data.cartItemCount)
                    error = null
                    quantity = 1
                    selectedSize = ""
                } else {
                    error = response.message ?: "Thêm vào giỏ hàng thất bại."
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

    LaunchedEffect(Unit) {
        fetchProductDetail()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (error!!.contains("502")) "Máy chủ tạm thời gặp sự cố, vui lòng thử lại sau." else "Lỗi: $error",
                    color = Color.Red,
                    textAlign = TextAlign.Center
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            modifier = Modifier
                                .clickable { outerNavController.popBackStack() }
                                .padding(top = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product!!.productName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(product!!.price),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF706B6B)
                        )
                    }

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

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                        Text("4.5", fontWeight = FontWeight.Bold)
                        Text("(15 Đánh giá)", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Mô tả", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = product!!.description,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Loại vải", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = product!!.fabric!!.fabricName,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Màu", fontWeight = FontWeight.SemiBold)
                    Row {
                        listOf(Color.White).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape)
                                    .padding(4.dp)
//                                    .clickable { /* chọn màu */ }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Kích thước", fontWeight = FontWeight.SemiBold)
                    Row {
                        product!!.sizes.forEach { size ->
                            OutlinedButton(
                                onClick = { selectedSize = size.size },
                                modifier = Modifier.padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedSize == size.size) Color(0xFFC4C3C3) else Color.Transparent
                                )
                            ) {
                                Text(size.size)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Tăng / giảm số lượng
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(
                                    0xFF000000
                                )
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("-", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = quantity.toString(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            Button(
                                onClick = { quantity++ },
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(
                                    0xFF000000
                                )
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            }
                        }




                    }
                }

                Button(
                    onClick = { handleAddToCart() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF050505))
                ) {
                    Text("Thêm vào giỏ hàng", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}