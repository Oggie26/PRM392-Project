package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.prm391_project.common.CartStateHolder
import com.example.prm391_project.config.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

data class CartDetail(
    val id: String,
    val productName: String,
    val price: String,
    val imageThumnail: String,
    val image: String,
    val quantity: Int,
    val color: String,
    val sizes: String,
    val description : String,

)

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    //Logic gọi API get detail
    val fetchProductDetail: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            error = null

            try {
                val response = RetrofitClient.productService.getProductDetail(productId)

                Log.d("ProductCartScreen", "Cart API Response Code: ${response.code}")
                Log.d("ProductCartScreen", "Cart API Response Message: ${response.message}")
                Log.d("ProductCartScreen", "Cart API Response Data: ${response.data}")

                if (response.code == 200) {
                    val product = response.data
                }

            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("ProductCartScreen", "HTTP Exception: ${e.message()}", e)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("ProductCartScreen", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("ProductCartScreen", "General Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

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

            try{
                val authHeader = "Bearer $token"
//                val response = RetrofitClient.cartService.addToCart(
//                    authHeader,
//                    productId,
//                    size,
//                    quantity
//                )

//                Log.d("ProductCartScreen", "Cart API Response Code: ${response.code}")
//                Log.d("ProductCartScreen", "Cart API Response Message: ${response.message}")
//                Log.d("ProductCartScreen", "Cart API Response Data: ${response.data}")

            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("ProductCartScreen", "HTTP Exception: ${e.message()}", e)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("ProductCartScreen", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("ProductCartScreen", "General Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchProductDetail()
    }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Back và Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Favorite")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ảnh sản phẩm
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter("https://link-to-your-image"),
                    contentDescription = "Product Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                // 3 dots indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Gray.copy(alpha = if (it == 0) 1f else 0.3f), CircleShape)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tên + Giá
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Nike Air Force", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("$50.00", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
            }

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                Text("4.5", fontWeight = FontWeight.Bold)
                Text("(15 Review)", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text("Details", fontWeight = FontWeight.SemiBold)
            Text(
                "Nike Dri-FIT is a polyester fabric designed to help you keep dry so you can more comfortably work harder, longer.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color Selection
            Text("Color", fontWeight = FontWeight.SemiBold)
            Row {
                listOf(Color.Black, Color(0xFFFFCC80), Color.LightGray).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color, CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                            .padding(4.dp)
                            .clickable { /* select color */ }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Size Dropdown (mock)
            Text("Size", fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                    .clickable { /* open size menu */ },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "CHOOSE SIZE",
                    modifier = Modifier.padding(start = 12.dp),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buy Now Button
            Button(
                onClick = { handleAddToCart() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Buy Now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }



}