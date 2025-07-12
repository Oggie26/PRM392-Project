package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.Canvas
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.R
import com.example.prm391_project.Screen
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.common.CartStateHolder
import com.example.prm391_project.response.CartResult
import com.example.prm391_project.response.CartItemDto
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int,
    val color: String?,
    val size: String?
)

fun CartItemDto.toCartItem(): CartItem? {
    return if (productId != null && productName != null && price != null && image != null && quantity != null) {
        CartItem(
            id = productId,
            name = productName,
            price = price,
            imageUrl = image,
            quantity = quantity,
            color = color,
            size = size
        )
    } else {
        null
    }
}

data class UpdateCartItemRequest(
    val quantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCartScreen(navController: NavController) {
    var cartItems by remember { mutableStateOf<MutableList<CartItem>>(mutableListOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var subtotal by remember { mutableStateOf(0.0) }
    var total by remember { mutableStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    // Add state for tracking items being deleted
    var deletingItems by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Danh sách các thay đổi số lượng tạm thời
    var pendingUpdates by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Logic gọi API để lấy giỏ hàng
    val fetchCartItems: () -> Unit = {
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

            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.cartService.getCart(authHeader)

                Log.d("ProductCartScreen", "Cart API Response Code: ${response.code}")
                Log.d("ProductCartScreen", "Cart API Response Message: ${response.message}")
                Log.d("ProductCartScreen", "Cart API Response Data: ${response.data}")

                if (response.code == 200) {
                    val cartResult = response.data
                    cartResult?.let { result ->
                        val fetchedItems = result.items?.mapNotNull { it.toCartItem() }?.toMutableList() ?: mutableListOf()
                        cartItems = fetchedItems
                        subtotal = result.totalPrice ?: 0.0
                        total = subtotal
                        CartStateHolder.updateCartItemCount(cartItems.size)
                        Log.d("ProductCartScreen", "Cart items loaded: ${cartItems.size}, total quantity: ${cartItems.sumOf { it.quantity }}")
                    } ?: run {
                        error = "Dữ liệu giỏ hàng trống hoặc không hợp lệ."
                        CartStateHolder.updateCartItemCount(0)
                        Log.e("ProductCartScreen", "CartResult is null even with code 200.")
                    }
                } else {
                    error = response.message ?: "Không thể tải giỏ hàng."
                    CartStateHolder.updateCartItemCount(0)
                    Log.e("ProductCartScreen", "API Error: ${response.code} - ${response.message}")
                }
            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("ProductCartScreen", "HTTP Exception: ${e.message()}", e)
                CartStateHolder.updateCartItemCount(0)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("ProductCartScreen", "IO Exception: ${e.message}", e)
                CartStateHolder.updateCartItemCount(0)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("ProductCartScreen", "General Exception: ${e.message}", e)
                CartStateHolder.updateCartItemCount(0)
            } finally {
                isLoading = false
            }
        }
    }

    // Hàm xóa sản phẩm khỏi giỏ hàng
    val deleteCartItem: (String) -> Unit = { productId ->
        coroutineScope.launch {
            deletingItems = deletingItems + (productId to true)
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(context, "Vui lòng đăng nhập để xóa sản phẩm.", Toast.LENGTH_SHORT).show()
                deletingItems = deletingItems - productId
                return@launch
            }

            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.cartService.removeItemsFromCart(authHeader, listOf(productId))

                if (response.code == 200) {
                    Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ hàng.", Toast.LENGTH_SHORT).show()
                    cartItems = cartItems.filterNot { it.id == productId }.toMutableList()
                    subtotal = cartItems.sumOf { it.price * it.quantity }
                    total = subtotal
                    CartStateHolder.updateCartItemCount(cartItems.size)
                } else {
                    Toast.makeText(context, "Lỗi xóa: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi xóa sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProductCartScreen", "Error deleting item: ${e.message}", e)
            } finally {
                deletingItems = deletingItems - productId
            }
        }
    }

    // Hàm cập nhật số lượng cục bộ và thêm vào pendingUpdates
    val updateQuantityLocally: (String, Int) -> Unit =
        updateQuantityLocally@{ productId, newQuantity ->
            if (newQuantity < 1) {
                Toast.makeText(context, "Số lượng không thể nhỏ hơn 1.", Toast.LENGTH_SHORT).show()
                return@updateQuantityLocally
            }
            cartItems = cartItems.map {
                if (it.id == productId) it.copy(quantity = newQuantity) else it
            }.toMutableList()
            subtotal = cartItems.sumOf { it.price * it.quantity }
            total = subtotal
            pendingUpdates = pendingUpdates + (productId to newQuantity)
        }

    // Hàm gọi API để đồng bộ số lượng khi chuyển tab/màn hình
    val syncCartWithServer: () -> Unit = {
        coroutineScope.launch {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(context, "Vui lòng đăng nhập để đồng bộ giỏ hàng.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            pendingUpdates.forEach { (productId, quantity) ->
                try {
                    val authHeader = "Bearer $token"
                    val response = RetrofitClient.cartService.updateQuantity(authHeader, productId, quantity)

                    if (response.code != 200) {
                        Toast.makeText(context, "Lỗi đồng bộ: ${response.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ProductCartScreen", "Sync failed for $productId: ${response.code} - ${response.message}")
                    }
                } catch (e: HttpException) {
                    Toast.makeText(context, "Lỗi HTTP khi đồng bộ: ${e.code()} - ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ProductCartScreen", "HTTP Exception during sync: ${e.message}", e)
                } catch (e: IOException) {
                    Toast.makeText(context, "Lỗi mạng khi đồng bộ.", Toast.LENGTH_SHORT).show()
                    Log.e("ProductCartScreen", "IO Exception during sync: ${e.message}", e)
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi không xác định khi đồng bộ.", Toast.LENGTH_SHORT).show()
                    Log.e("ProductCartScreen", "General Exception during sync: ${e.message}", e)
                }
            }
            pendingUpdates = emptyMap() // Xóa các thay đổi đã đồng bộ
            fetchCartItems() // Làm mới giỏ hàng từ server
        }
    }

    // Gọi API khi màn hình được khởi tạo
    LaunchedEffect(Unit) {
        fetchCartItems()
    }

    // Đồng bộ khi màn hình bị dispose (chuyển tab/màn hình)
    DisposableEffect(Unit) {
        onDispose {
            syncCartWithServer()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Giỏ hàng",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    lineHeight = 28.sp
                )
                Text(
                    text = "Của Tôi",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Lỗi: $error", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                } else if (cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Giỏ hàng của bạn đang trống!", textAlign = TextAlign.Center)
                    }
                } else {
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
                                    updateQuantityLocally(item.id, newQuantity)
                                },
                                onDelete = {
                                    deleteCartItem(item.id)
                                },
                                isDeleting = deletingItems[item.id] == true,
                                onItemClick = {
                                    navController.navigate(Screen.ProductDetail.createRoute(item.id))
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
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
                                text = "Tổng",
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                syncCartWithServer()
                                navController.navigate("home")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Text(
                                text = "Mua tiếp",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        Button(
                            onClick = {
                                syncCartWithServer()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Thanh toán",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
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
    onDelete: () -> Unit,
    isDeleting: Boolean,
    onItemClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipeDistance = 80.dp
    val density = LocalDensity.current
    val maxSwipeDistancePx = with(density) { maxSwipeDistance.toPx() }

    val draggableState = rememberDraggableState { delta ->
        val newOffset = offsetX + delta
        offsetX = newOffset.coerceIn(-maxSwipeDistancePx, 0f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDeleting) { onItemClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(Color(0xFFFF4444), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 16.dp),
                    strokeWidth = 2.dp
                )
            } else {
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
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        if (offsetX > -maxSwipeDistancePx / 2) {
                            offsetX = 0f
                        } else {
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
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            if (!item.color.isNullOrEmpty() || !item.size.isNullOrEmpty()) {
                Text(
                    text = buildString {
                        if (!item.color.isNullOrEmpty()) append(item.color)
                        if (!item.color.isNullOrEmpty() && !item.size.isNullOrEmpty()) append(" / ")
                        if (!item.size.isNullOrEmpty()) append(item.size)
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = formatter.format(item.price),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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