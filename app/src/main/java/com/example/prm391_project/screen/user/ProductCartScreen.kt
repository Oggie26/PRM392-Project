package com.example.prm391_project.screen.user

import TokenManager // Import TokenManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
// import com.example.prm391_project.common.CartItem // <-- Có thể không cần nữa nếu dùng CartItemDto trực tiếp
// import com.example.prm391_project.common.MockData // <-- LOẠI BỎ IMPORT NÀY
import coil.compose.AsyncImage
import com.example.prm391_project.config.RetrofitClient
// import com.example.prm391_project.screen.user.components.CustomTopBar // Bỏ comment nếu bạn muốn dùng
import java.text.NumberFormat
import java.util.*

// IMPORTS MỚI CHO API
import com.example.prm391_project.response.CartResult
import com.example.prm391_project.response.CartItemDto // Import CartItemDto
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Nếu bạn vẫn muốn giữ CartItem cho UI (tốt hơn là dùng CartItemDto trực tiếp hoặc chuyển đổi)
// thì bạn có thể tạo một extension function để chuyển đổi từ CartItemDto sang CartItem.
// Ví dụ:
data class CartItem( // Giữ lại nếu bạn vẫn muốn cấu trúc này cho UI
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int
)

fun CartItemDto.toCartItem(): CartItem? {
    return if (id != null && name != null && price != null && imageUrl != null && quantity != null) {
        CartItem(id, name, price, imageUrl, quantity)
    } else {
        null // Trả về null nếu có bất kỳ trường nào bị thiếu
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCartScreen(navController: NavController) {
    // Thay đổi từ MockData sang dữ liệu từ API
    var cartItems by remember { mutableStateOf<MutableList<CartItem>>(mutableListOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var subtotal by remember { mutableStateOf(0.0) }
    var total by remember { mutableStateOf(0.0) } // total có thể bằng subtotal nếu chưa có discount
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Logic gọi API để lấy giỏ hàng
    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            error = "Bạn chưa đăng nhập. Vui lòng đăng nhập để xem giỏ hàng."
            isLoading = false
            // Có thể điều hướng về LoginScreen
            // navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            return@LaunchedEffect
        }

        coroutineScope.launch {
            try {
                val authHeader = "Bearer $token"
                // THAY ĐỔI DÒNG NÀY:
                val response = RetrofitClient.cartService.getCart(authHeader) // <-- SỬ DỤNG cartService

                Log.d("ProductCartScreen", "Cart API Response Code: ${response.code}")
                Log.d("ProductCartScreen", "Cart API Response Message: ${response.message}")

                if (response.code == 200) {
                    val cartResult = response.data
                    cartResult?.let { result ->
                        cartItems = result.items?.mapNotNull { it.toCartItem() }?.toMutableList() ?: mutableListOf()
                        subtotal = result.totalPrice ?: 0.0
                        total = subtotal
                    }
                } else {
                    error = response.message ?: "Không thể tải giỏ hàng."
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Thêm background cho toàn bộ màn hình
    ) {
        // Top Bar
        // Bỏ comment nếu bạn muốn dùng CustomTopBar
        // CustomTopBar(
        //     title = "Cart",
        //     onBackClick = { navController.popBackStack() }
        // )

        // Content với padding và spacing tốt hơn
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Padding hai bên
            shape = RoundedCornerShape( 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp) // Giảm padding
            ) {
                // Title
                Text(
                    text = "My",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    lineHeight = 28.sp
                )
                Text(
                    text = "Cart List",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Hiển thị trạng thái tải/lỗi/dữ liệu
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text("Đang tải giỏ hàng...", modifier = Modifier.padding(top = 60.dp))
                    }
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Lỗi: $error", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                } else if (cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Giỏ hàng của bạn đang trống!", textAlign = TextAlign.Center)
                    }
                } else {
                    // Cart Items - chiếm phần lớn không gian
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cartItems, key = { it.id }) { item ->
                            SwipeableCartItem(
                                item = item,
                                onQuantityChange = { newQuantity ->
                                    cartItems = cartItems.map {
                                        if (it.id == item.id) it.copy(quantity = newQuantity)
                                        else it
                                    }.toMutableList()
                                    // Cập nhật lại tổng tiền sau khi thay đổi số lượng
                                    subtotal = cartItems.sumOf { it.price * it.quantity }
                                    total = subtotal
                                },
                                onDelete = {
                                    cartItems = cartItems.filter { it.id != item.id }.toMutableList()
                                    // Cập nhật lại tổng tiền sau khi xóa item
                                    subtotal = cartItems.sumOf { it.price * it.quantity }
                                    total = subtotal
                                }
                            )
                        }

                        // Thêm spacer cuối để có thêm không gian cuộn
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }


                // Bottom Section - cố định ở cuối
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Price Summary
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = formatter.format(subtotal),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Divider
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        ) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.5f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = formatter.format(total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Checkout Button
                    Button(
                        onClick = { /* Handle checkout */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Checkout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Continue Shopping Button
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(
                            text = "Continue Shopping",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableCartItem(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipeDistance = 80.dp
    val density = LocalDensity.current
    val maxSwipeDistancePx = with(density) { maxSwipeDistance.toPx() }

    val draggableState = rememberDraggableState { delta ->
        val newOffset = offsetX + delta
        // Chỉ cho phép kéo sang trái (offset âm) và giới hạn khoảng cách
        offsetX = newOffset.coerceIn(-maxSwipeDistancePx, 0f)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Delete background - luôn hiển thị phía sau
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp) // Chiều cao của cart item
                .background(Color(0xFFFF4444), RoundedCornerShape(12.dp)), // Màu đỏ đẹp hơn
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Cart item content - có thể kéo được
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        // Khi thả tay, nếu kéo ít hơn một nửa thì tự động đóng lại
                        if (offsetX > -maxSwipeDistancePx / 2) {
                            offsetX = 0f
                        } else {
                            // Nếu kéo nhiều thì giữ ở vị trí mở
                            offsetX = -maxSwipeDistancePx
                        }
                    }
                )
        ) {
            CartItemRow(
                item = item,
                onQuantityChange = onQuantityChange
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8F8F8))
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Product Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = formatter.format(item.price),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Quantity Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Plus Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onQuantityChange(item.quantity + 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.quantity.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Minus Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        if (item.quantity > 1) {
                            onQuantityChange(item.quantity - 1)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}